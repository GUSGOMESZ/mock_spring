package br.com.valueprojects.mock_spring.service;

import br.com.valueprojects.mock_spring.model.Jogo;

public class GerenciadorSMS {
    
    public void enviaParaVencedor(Jogo jogo) {
        if (jogo.getResultados().isEmpty()) {
            throw new IllegalArgumentException("Jogo sem resultados");
        }
        System.out.println("SMS enviado para vencedor do jogo: " + jogo.getDescricao());
    }
}
