package com.estoque;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
            // Cria um arquivo temporário para armazenar o conteúdo do arquivo
            File tempFile = File.createTempFile("estoque_", ".xls");
            file.transferTo(tempFile);

            // Abre o arquivo Excel
            FileInputStream fis = new FileInputStream(tempFile);
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0); // Pega a primeira planilha

            // Lista para armazenar os dados processados
            List<String[]> dadosProcessados = new ArrayList<>();

            // Itera sobre as linhas da planilha
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                // Ignora a primeira linha (cabeçalho)
                if (row.getRowNum() == 0) {
                    continue;
                }

                // Obtém as células da linha
                Cell cellDescricao = row.getCell(0); // Coluna A (Descrição)
                Cell cellEstoque = row.getCell(1);  // Coluna B (Estoque físico)

                // Verifica se as células não são nulas
                if (cellDescricao == null || cellEstoque == null) {
                    continue;
                }

                // Extrai os valores das células
                String descricao = cellDescricao.getStringCellValue();
                String estoque = String.valueOf(cellEstoque.getNumericCellValue());

                // Extrai cor e tamanho da descrição
                String cor = obterDetalhe(descricao, "Cor:");
                String tamanho = obterDetalhe(descricao, "Tamanho:");

                // Adiciona os dados processados à lista
                dadosProcessados.add(new String[]{cor, tamanho, estoque});
            }

            // Fecha o workbook e o FileInputStream
            workbook.close();
            fis.close();

            return ResponseEntity.ok(dadosProcessados);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar arquivo: " + e.getMessage());
        }
    }

    // Método auxiliar para extrair o detalhe de "Cor:" ou "Tamanho:"
    private String obterDetalhe(String descricao, String chave) {
        // Remove as aspas da descrição, se houver
        descricao = descricao.replaceAll("\"", "");

        // Verifica se a chave existe na descrição
        if (!descricao.contains(chave)) {
            return "Desconhecido"; // Retorna "Desconhecido" se a chave não for encontrada
        }

        // Procura pela chave na descrição
        int indiceChave = descricao.indexOf(chave);
        if (indiceChave == -1) {
            return "Desconhecido"; // Retorna "Desconhecido" se a chave não for encontrada
        }

        // Extrai o valor após a chave
        String valor = descricao.substring(indiceChave + chave.length());

        // Remove qualquer texto após o próximo ponto e vírgula (;)
        int indicePontoEVirgula = valor.indexOf(";");
        if (indicePontoEVirgula != -1) {
            valor = valor.substring(0, indicePontoEVirgula).trim();
        }

        return valor.trim(); // Retorna o valor extraído
    }
}