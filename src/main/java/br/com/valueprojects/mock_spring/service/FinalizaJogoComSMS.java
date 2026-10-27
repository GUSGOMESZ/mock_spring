package br.com.valueprojects.mock_spring.service;

import br.com.valueprojects.mock_spring.model.Jogo;
import infra.JogoDao;
import java.util.List;

public class FinalizaJogoComSMS {
    
    private final JogoDao dao;
    private final GerenciadorSMS sms;
    
    public FinalizaJogoComSMS(JogoDao dao, GerenciadorSMS sms) {
        this.dao = dao;
        this.sms = sms;
    }
    
    public void finalizaENotifica() {
        List<Jogo> jogos = dao.emAndamento();
        
        for (Jogo jogo : jogos) {
            // PASSO 1: Salvar na base (SEMPRE PRIMEIRO)
            dao.atualiza(jogo);
            
            // PASSO 2: Enviar SMS (DEPOIS)
            sms.enviaParaVencedor(jogo);
        }
    }
}
