package com.estoque;

import org.apache.commons.csv.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {

     @GetMapping("/keep-alive")
    public ResponseEntity<String> keepAlive() {
        return ResponseEntity.ok("API está ativa");
    }

    @PostMapping("/upload")
    public List<Map<String, String>> uploadCSV(@RequestParam("file") MultipartFile file) {
        List<Map<String, String>> produtos = new ArrayList<>();

        if (file.isEmpty()) {
            throw new RuntimeException("Erro: O arquivo CSV está vazio!");
        }

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            System.out.println("Arquivo recebido: " + file.getOriginalFilename());

            CSVParser csvParser = CSVFormat.DEFAULT
                .withDelimiter(';') // Teste com ',' se necessário
                .withFirstRecordAsHeader()
                .withIgnoreSurroundingSpaces()
                .parse(reader);

            // Exibir cabeçalhos detectados para depuração
            System.out.println("Cabeçalhos encontrados:");
            for (String header : csvParser.getHeaderMap().keySet()) {
                System.out.println("'" + header + "'");
            }

            for (CSVRecord record : csvParser) {
                Map<String, String> produto = new HashMap<>();

                try {
                    // Captura os nomes exatos dos cabeçalhos
                    String descricaoHeader = csvParser.getHeaderMap().keySet().stream()
                        .filter(h -> h.trim().equalsIgnoreCase("Descrição"))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Coluna 'Descrição' não encontrada!"));

                    String estoqueHeader = csvParser.getHeaderMap().keySet().stream()
                        .filter(h -> h.trim().equalsIgnoreCase("Estoque"))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Coluna 'Estoque' não encontrada!"));

                    // Obtém os valores das colunas
                    String descricao = record.get(descricaoHeader).trim();
                    String estoque = record.get(estoqueHeader).trim();

                    produto.put("descricao", descricao);
                    produto.put("estoque", estoque);
                    produtos.add(produto);

                    System.out.println("Produto adicionado: " + produto);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Erro: Verifique se os nomes das colunas do CSV são exatamente 'Descrição' e 'Estoque'.", e);
                }
            }

            System.out.println("Processamento do CSV concluído com sucesso!");

        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar o arquivo CSV: " + e.getMessage(), e);
        }

        return produtos;
    }
}
