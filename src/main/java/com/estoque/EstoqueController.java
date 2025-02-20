package com.estoque;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {
    @PostMapping("/upload")
    public ResponseEntity<?> uploadArquivo(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Arquivo vazio!");
        }
        
        try {
            File tempFile = File.createTempFile("estoque_", ".csv");
            file.transferTo(tempFile);
            
            List<String> linhas = Files.readAllLines(Paths.get(tempFile.getAbsolutePath()));
            List<String[]> dadosProcessados = linhas.stream()
                    .skip(1) // Ignorar cabeçalho
                    .map(linha -> linha.split(";"))
                    .map(campos -> {
                        String[] detalhes = campos[0].split(",?");
                        String cor = detalhes[0].replace("Cor:", "").trim();
                        String tamanho = detalhes[1].replace("Tamanho:", "").trim();
                        String estoque = campos[1].trim();
                        return new String[]{cor, tamanho, estoque};
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dadosProcessados);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar arquivo");
        }
    }
}
