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
<<<<<<< HEAD

            // Verifica se o arquivo tem pelo menos uma linha além do cabeçalho
            if (linhas.size() <= 1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Arquivo sem dados!");
            }

=======
>>>>>>> 1f1012b817428b749879905125c374dc5601acc5
            List<String[]> dadosProcessados = linhas.stream()
                    .skip(1) // Ignorar cabeçalho
                    .map(linha -> linha.replaceAll("\"", "").split(";")) // Remove aspas e separa corretamente
                    .map(campos -> {
<<<<<<< HEAD
                        // Garantir que o array tenha pelo menos 6 colunas
                        if (campos.length < 6) {
                            return new String[]{"Erro", "Dados inválidos", "0"};
                        }

                        String descricao = campos[1].trim(); // Descrição do produto
                        String estoque = campos[5].replace(",", ".").trim(); // Estoque físico

                        // Se o estoque estiver vazio, definir como "0"
                        if (estoque.isEmpty()) {
                            estoque = "0";
                        }
=======
                        if (campos.length < 2) return new String[]{"Erro", "Dados inválidos", "0"};

                        String descricao = campos[0];
                        String estoque = campos[1].replace(",", ".").trim(); // Substituir vírgula por ponto para evitar erros numéricos
>>>>>>> 1f1012b817428b749879905125c374dc5601acc5

                        String cor = "Desconhecido";
                        String tamanho = "N/A";

<<<<<<< HEAD
                        // Extração robusta da Cor e Tamanho
                        if (descricao.contains("Cor:")) {
                            String[] partes = descricao.split("Cor:");
                            if (partes.length > 1) {
                                cor = partes[1].split(";")[0].trim();
                            }
                        }

                        if (descricao.contains("Tamanho:")) {
                            String[] partes = descricao.split("Tamanho:");
                            if (partes.length > 1) {
                                tamanho = partes[1].split(";")[0].trim();
=======
                        for (String detalhe : descricao.split(";")) {
                            if (detalhe.trim().startsWith("Cor:")) {
                                cor = detalhe.replace("Cor:", "").trim();
                            } else if (detalhe.trim().startsWith("Tamanho:")) {
                                tamanho = detalhe.replace("Tamanho:", "").trim();
>>>>>>> 1f1012b817428b749879905125c374dc5601acc5
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
