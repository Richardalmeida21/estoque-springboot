package com.estoque;

import org.apache.commons.csv.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    @PostMapping("/upload")
    public List<Map<String, String>> uploadCSV(@RequestParam("file") MultipartFile file) {
        List<Map<String, String>> produtos = new ArrayList<>();
        
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            CSVParser csvParser = CSVFormat.DEFAULT.withDelimiter(';').withFirstRecordAsHeader().parse(reader);
            
            for (CSVRecord record : csvParser) {
                Map<String, String> produto = new HashMap<>();
                produto.put("descricao", record.get("Descrição"));
                produto.put("estoque", record.get("Estoque"));
                produtos.add(produto);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar o arquivo CSV", e);
        }
        
        return produtos;
    }
}
