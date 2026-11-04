package br.com.valueprojects.mock_spring.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import br.com.valueprojects.mock_spring.service.FinalizaJogoComSMS;
import infra.JogoDaoEmMemoria;

/**
 * Pratica 4: teste de integracao da observabilidade.
 * @AutoConfigureObservability liga o registry do Prometheus nos testes
 * (por padrao o Spring Boot desliga a exportacao de metricas em testes).
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureObservability
public class FinalizacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JogoDaoEmMemoria dao;

    @Autowired
    private FinalizaJogoComSMS finalizador;

    @BeforeEach
    public void limparEstado() {
        dao.setProbabilidadeFalha(0.0);
        while (dao.quantidadeEmAndamento() > 0) {
            try {
                finalizador.finalizaENotifica();
            } catch (RuntimeException e) {
                // jogos sem resultado falham no SMS, mas ja foram salvos
            }
        }
    }

    @Test
    public void deveFinalizarJogosEExporMetricasNoPrometheus() throws Exception {
        mockMvc.perform(post("/simulacao/jogos").param("quantidade", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emAndamento").value(3));

        mockMvc.perform(post("/jogos/finalizar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.emAndamento").value(0));

        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("jogos_processados_total")))
                .andExpect(content().string(containsString("sms_enviados_total")))
                .andExpect(content().string(containsString("jogos_falhas_salvar_total")))
                .andExpect(content().string(containsString("jogos_finalizacao_tempo_seconds_count")))
                .andExpect(content().string(containsString("jogos_em_andamento")));
    }

    @Test
    public void deveRetornarErroQuandoSmsFalhar() throws Exception {
        mockMvc.perform(post("/simulacao/jogos").param("quantidade", "1").param("semResultado", "1"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/jogos/finalizar"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("ERRO"));
    }

    @Test
    public void deveRetornarErroQuandoFalharAoSalvar() throws Exception {
        mockMvc.perform(post("/simulacao/jogos").param("quantidade", "1"));
        mockMvc.perform(post("/simulacao/falhas").param("probabilidade", "1.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.probabilidadeFalha").value(1.0));

        mockMvc.perform(post("/jogos/finalizar"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void deveRejeitarProbabilidadeInvalida() throws Exception {
        mockMvc.perform(post("/simulacao/falhas").param("probabilidade", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void healthDeveEstarUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
