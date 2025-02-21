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
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {

    private static final Logger logger = Logger.getLogger(EstoqueController.class.getName());

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
        } catch (Exception e) {
            logger.severe("Erro ao processar arquivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno: " + e.getMessage());
        }
    }

    private List<Map<String, String>> processarExcel(MultipartFile file) throws IOException {
        InputStream is = file.getInputStream();
        Workbook workbook;

        if (file.getOriginalFilename().endsWith(".xls")) {
            workbook = new HSSFWorkbook(is);
        } else {
            workbook = new XSSFWorkbook(is);
        }

        Sheet sheet = workbook.getSheetAt(0);
        List<Map<String, String>> dadosProcessados = new ArrayList<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;
            
            Cell cellDescricao = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Cell cellEstoque = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

            String descricao = cellDescricao.toString().trim();
            logger.info("Descrição lida: " + descricao);
            
            String estoqueStr = cellEstoque.toString().replace(",", ".").trim();
            
            double estoque;
            try {
                estoque = Double.parseDouble(estoqueStr);
            } catch (NumberFormatException e) {
                estoque = 0.0;
            }

            Map<String, String> item = new HashMap<>();
            item.put("cor", obterDetalhe(descricao, "Cor:"));
            item.put("tamanho", obterDetalhe(descricao, "Tamanho:"));
            item.put("estoque", String.valueOf(estoque));
            
            dadosProcessados.add(item);
        }

        workbook.close();
        return dadosProcessados;
    }

    private List<Map<String, String>> processarCSV(MultipartFile file) throws IOException {
        List<Map<String, String>> dadosProcessados = new ArrayList<>();
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

            String descricao = partes[0].replaceAll("\"", "").trim();
            logger.info("Descrição lida: " + descricao);
            
            String estoqueStr = partes[1].replace(",", ".").replaceAll("\"", "").trim();
            
            double estoque;
            try {
                estoque = Double.parseDouble(estoqueStr);
            } catch (NumberFormatException e) {
                estoque = 0.0;
            }

            Map<String, String> item = new HashMap<>();
            item.put("cor", obterDetalhe(descricao, "Cor:"));
            item.put("tamanho", obterDetalhe(descricao, "Tamanho:"));
            item.put("estoque", String.valueOf(estoque));
            
            dadosProcessados.add(item);
        }
        return dadosProcessados;
    }

    private String obterDetalhe(String descricao, String chave) {
        descricao = descricao.replaceAll("\"", "").trim();
        
        int indiceChave = descricao.indexOf(chave);
        if (indiceChave == -1) return "Desconhecido";

        String valor = descricao.substring(indiceChave + chave.length()).trim();

        // Capturar o primeiro espaço, ponto e vírgula ou fim da string
        String[] partes = valor.split("[;\s]", 2);
        
        return partes[0].trim();
    }
}
