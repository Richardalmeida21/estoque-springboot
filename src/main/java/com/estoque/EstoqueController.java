package com.estoque;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.IOException;
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
                        if (campos.length < 6) return new String[]{"Erro", "Dados inválidos", "0"};

                        String descricao = campos[1]; // A descrição está na segunda coluna
                        String estoque = campos[5].replace(",", ".").trim(); // O estoque está na sexta coluna

                        // Se o estoque estiver vazio ou inválido, define como 0
                        if (estoque.isEmpty() || !estoque.matches("[0-9]*[.,]?[0-9]+")) {
                            estoque = "0";
                        }

                        // Extrai cor e tamanho da descrição
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

    private String obterDetalhe(String descricao, String chave) {
        descricao = descricao.replaceAll("\"", "");
        System.out.println("Processando descrição: " + descricao + " com chave: " + chave);

        int indiceChave = descricao.indexOf(chave);
        if (indiceChave == -1) {
            System.out.println("Chave não encontrada: " + chave);
            return "Desconhecido";
        }

        String valor = descricao.substring(indiceChave + chave.length());
        int indicePontoEVirgula = valor.indexOf(";");
        if (indicePontoEVirgula != -1) {
            valor = valor.substring(0, indicePontoEVirgula).trim();
        }

        System.out.println("Valor extraído: " + valor);
        return valor.trim();
    }
}