package br.com.valueprojects.mock_spring.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.model.Participante;
import br.com.valueprojects.mock_spring.model.Resultado;
import br.com.valueprojects.mock_spring.service.FinalizaJogoComSMS;
import infra.JogoDaoEmMemoria;

/**
 * Pratica 4: endpoints para exercitar a regra de negocio com a aplicacao
 * rodando e assim gerar metricas reais no Prometheus/Grafana.
 */
@RestController
public class FinalizacaoController {

    private final FinalizaJogoComSMS finalizador;
    private final JogoDaoEmMemoria dao;

    public FinalizacaoController(FinalizaJogoComSMS finalizador, JogoDaoEmMemoria dao) {
        this.finalizador = finalizador;
        this.dao = dao;
    }

    /** Executa a finalizacao: salva cada jogo e depois envia o SMS ao vencedor. */
    @PostMapping("/jogos/finalizar")
    public ResponseEntity<Map<String, Object>> finalizar() {
        try {
            finalizador.finalizaENotifica();
            return ResponseEntity.ok(Map.of("status", "OK", "emAndamento", dao.quantidadeEmAndamento()));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "ERRO", "mensagem", e.getMessage()));
        }
    }

    /** Cria jogos de exemplo. Jogos "semResultado" fazem o envio de SMS falhar. */
    @PostMapping("/simulacao/jogos")
    public Map<String, Object> criarJogos(@RequestParam(defaultValue = "5") int quantidade,
                                          @RequestParam(defaultValue = "0") int semResultado) {
        for (int i = 0; i < quantidade; i++) {
            Jogo jogo = new Jogo("Jogo simulado " + i);
            if (i >= semResultado) {
                jogo.anota(new Resultado(new Participante(i, "Jogador " + i), 100.0));
            }
            dao.salva(jogo);
        }
        return Map.of("criados", quantidade, "emAndamento", dao.quantidadeEmAndamento());
    }

    /** Liga/desliga falhas simuladas ao salvar (0.0 a 1.0). */
    @PostMapping("/simulacao/falhas")
    public ResponseEntity<Map<String, Object>> configurarFalhas(@RequestParam double probabilidade) {
        try {
            dao.setProbabilidadeFalha(probabilidade);
            return ResponseEntity.ok(Map.of("probabilidadeFalha", dao.getProbabilidadeFalha()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", e.getMessage()));
        }
    }
}
