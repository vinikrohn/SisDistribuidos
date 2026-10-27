package utilidades;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import model.Sessao;

/**
 * Mesma rotina do sistema local: lê o arquivo "numero;dadosJSON"
 * e alimenta o HashMap (chave = número do celular).
 */
public class Util {

    public static String carregarArquivoEmHashMap(String nomeArquivo, HashMap<String, Sessao> sessoes, StringBuilder dadosSessoes) {
        try {
            Path caminho = Paths.get(nomeArquivo);

            // 1. Lê todas as linhas do arquivo de uma vez só
            List<String> linhas = Files.readAllLines(caminho, StandardCharsets.UTF_8);

            // 2. Alimenta o StringBuilder e o HashMap em um único fluxo
            for (String linha : linhas) {
                dadosSessoes.append(linha).append(System.lineSeparator());

                if (!linha.trim().isEmpty()) {
                    // limit 2: o JSON da sessão fica inteiro, mesmo que contenha ';'
                    String[] dadosLinha = linha.split(";", 2);

                    if (dadosLinha.length >= 2) {
                        Sessao sessao = new Sessao(dadosLinha[0].trim(), dadosLinha[1].trim());
                        sessoes.put(sessao.getNumeroCelular(), sessao);
                    }
                }
            }
            return "";
        } catch (IOException e) {
            return "Erro ao processar o arquivo: " + e.getMessage();
        }
    }
}
