package com.estoque;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {

    @GetMapping("/")
    public String home() {
        return "API do sistema de estoque está rodando!";
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadArquivo(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Arquivo vazio!");
        }

        try {
            String fileName = file.getOriginalFilename();
            if (fileName != null && fileName.endsWith(".csv")) {
                return ResponseEntity.ok(processarCSV(file));
            } else {
                return ResponseEntity.ok(processarExcel(file));
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar arquivo: " + e.getMessage());
        }
    }

    private List<String[]> processarExcel(MultipartFile file) throws IOException {
        InputStream is = file.getInputStream();
        Workbook workbook;

        if (file.getOriginalFilename().endsWith(".xls")) {
            workbook = new HSSFWorkbook(is);
        } else {
            workbook = new XSSFWorkbook(is);
        }

        Sheet sheet = workbook.getSheetAt(0);
        List<String[]> dadosProcessados = new ArrayList<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;
            
            Cell cellDescricao = row.getCell(0);
            Cell cellEstoque = row.getCell(1);

            if (cellDescricao == null || cellEstoque == null) continue;
            
            String descricao = cellDescricao.getStringCellValue();
            double estoque = cellEstoque.getNumericCellValue();

            String cor = obterDetalhe(descricao, "Cor:");
            String tamanho = obterDetalhe(descricao, "Tamanho:");
            
            dadosProcessados.add(new String[]{cor, tamanho, String.valueOf(estoque)});
        }

        workbook.close();
        return dadosProcessados;
    }

    private List<String[]> processarCSV(MultipartFile file) throws IOException {
        List<String[]> dadosProcessados = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
        
        String linha;
        boolean primeiraLinha = true;
        while ((linha = br.readLine()) != null) {
            if (primeiraLinha) { 
                primeiraLinha = false;
                continue;
            }
            String[] partes = linha.split(";");
            if (partes.length < 2) continue;

            String descricao = partes[0];
            String estoqueStr = partes[1].replace(",", ".").replaceAll("\"", "").trim();
            double estoque = estoqueStr.isEmpty() ? 0.0 : Double.parseDouble(estoqueStr);


            String cor = obterDetalhe(descricao, "Cor:");
            String tamanho = obterDetalhe(descricao, "Tamanho:");

            dadosProcessados.add(new String[]{cor, tamanho, String.valueOf(estoque)});
        }
        return dadosProcessados;
    }

    private String obterDetalhe(String descricao, String chave) {
        descricao = descricao.replaceAll("\"", "");
        if (!descricao.contains(chave)) return "Desconhecido";
        
        int indiceChave = descricao.indexOf(chave);
        if (indiceChave == -1) return "Desconhecido";
        
        String valor = descricao.substring(indiceChave + chave.length());
        int indicePontoEVirgula = valor.indexOf(";");
        if (indicePontoEVirgula != -1) {
            valor = valor.substring(0, indicePontoEVirgula).trim();
        }
        return valor.trim();
    }
}
