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
            File tempFile = File.createTempFile("estoque_", ".csv");
            file.transferTo(tempFile);

            List<String> linhas = Files.readAllLines(Paths.get(tempFile.getAbsolutePath()));
            List<String[]> dadosProcessados = linhas.stream()
                    .skip(1) // Ignorar cabeçalho
                    .map(linha -> linha.replaceAll("\"", "").split(";")) // Remove aspas e separa corretamente
                    .map(campos -> {
                        if (campos.length < 2) return new String[]{"Erro", "Dados inválidos", "0"};

                        String descricao = campos[0];
                        String estoque = campos[1].replace(",", ".").trim(); // Substituir vírgula por ponto para evitar erros numéricos

                        String cor = "Desconhecido";
                        String tamanho = "N/A";

                        for (String detalhe : descricao.split(";")) {
                            if (detalhe.trim().startsWith("Cor:")) {
                                cor = detalhe.replace("Cor:", "").trim();
                            } else if (detalhe.trim().startsWith("Tamanho:")) {
                                tamanho = detalhe.replace("Tamanho:", "").trim();
                            }
                        }

                        return new String[]{cor, tamanho, estoque};
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dadosProcessados);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar arquivo");
        }
    }
}
