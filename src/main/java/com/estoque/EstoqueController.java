package com.estoque;

import org.apache.commons.csv.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {

    @PostMapping("/upload")
    public List<Map<String, String>> uploadCSV(@RequestParam("file") MultipartFile file) {
        List<Map<String, String>> produtos = new ArrayList<>();

        if (file.isEmpty()) {
            throw new RuntimeException("Erro: O arquivo CSV está vazio!");
        }

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            System.out.println("Arquivo recebido: " + file.getOriginalFilename());

            CSVParser csvParser = CSVFormat.DEFAULT
                .withDelimiter(';')
                .withFirstRecordAsHeader()
                .withIgnoreSurroundingSpaces()
                .parse(reader);

            for (CSVRecord record : csvParser) {
                Map<String, String> produto = new HashMap<>();

                try {
                    String descricao = record.get("Descrição").trim();
                    String estoque = record.get("Estoque").trim();

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
