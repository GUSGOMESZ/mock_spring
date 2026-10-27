package br.com.valueprojects.mock_spring.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Calendar;

import org.junit.jupiter.api.Test;

import br.com.valueprojects.mock_spring.builder.CriadorDeJogo;
import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.model.Participante;
import br.com.valueprojects.mock_spring.model.Resultado;

public class GerenciadorSMSTest {
    
    @Test
    public void deveEnviarSMSComSucesso() {
        GerenciadorSMS gerenciador = new GerenciadorSMS();
        
        Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);
        
        Jogo jogo = new CriadorDeJogo().para("Cata moedas")
            .naData(antiga).constroi();
        jogo.setId(1);
        jogo.finaliza();
        
        Participante participante = new Participante(1, "Jogador 1");
        Resultado resultado = new Resultado(participante, 100.0);
        jogo.anota(resultado);
        
        // Não deve lançar exceção
        assertDoesNotThrow(() -> gerenciador.enviaParaVencedor(jogo));
    }
    
    @Test
    public void deveLancarExcecaoQuandoJogoSemResultados() {
        GerenciadorSMS gerenciador = new GerenciadorSMS();
        
        Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);
        
        Jogo jogo = new CriadorDeJogo().para("Jogo vazio")
            .naData(antiga).constroi();
        jogo.setId(1);
        jogo.finaliza();
        
        // Deve lançar IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> 
            gerenciador.enviaParaVencedor(jogo)
        );
    }
}
