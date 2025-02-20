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
            // Cria um arquivo temporário para armazenar o conteúdo do arquivo
            File tempFile = File.createTempFile("estoque_", ".csv");
            file.transferTo(tempFile);

            // Lê todas as linhas do arquivo CSV
            List<String> linhas = Files.readAllLines(Paths.get(tempFile.getAbsolutePath()));

            // Verifica se o arquivo tem pelo menos uma linha além do cabeçalho
            if (linhas.size() <= 1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Arquivo sem dados!");
            }

            // Processa as linhas do arquivo CSV
            List<String[]> dadosProcessados = linhas.stream()
                    .skip(1) // Ignora o cabeçalho
                    .map(linha -> linha.replaceAll("\"", "").split(";")) // Remove aspas e separa corretamente
                    .map(campos -> {
                        // Verifica se o número de colunas é adequado
                        if (campos.length < 2) return new String[]{"Erro", "Dados inválidos", "0"};

                        String descricao = campos[0];
                        String estoque = campos[1].replace(",", ".").trim(); // Substitui a vírgula por ponto para valores numéricos

                        // Se o estoque estiver vazio ou inválido, define como 0
                        if (estoque.isEmpty() || !estoque.matches("[0-9]*[.,]?[0-9]+")) {
                            estoque = "0";
                        }

                        // Definição dos valores padrão para cor e tamanho
                        String cor = obterDetalhe(descricao, "Cor:");
                        String tamanho = obterDetalhe(descricao, "Tamanho:");

                        return new String[]{cor, tamanho, estoque};
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dadosProcessados);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar arquivo: " + e.getMessage());
        }
    }

    // Método auxiliar para extrair o detalhe de "Cor:" ou "Tamanho:"
    private String obterDetalhe(String descricao, String chave) {
        for (String detalhe : descricao.split(";")) {
            if (detalhe.trim().startsWith(chave)) {
                return detalhe.replace(chave, "").trim();
            }
        }
        return "Desconhecido"; // Retorna "Desconhecido" caso não encontre
    }
}
