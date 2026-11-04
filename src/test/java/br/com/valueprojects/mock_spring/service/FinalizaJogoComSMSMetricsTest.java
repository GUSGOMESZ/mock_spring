package br.com.valueprojects.mock_spring.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.valueprojects.mock_spring.model.Jogo;
import infra.RepositorioDeJogos;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Pratica 4: garante que as metricas de negocio refletem o que aconteceu.
 * Usa SimpleMeterRegistry (em memoria) + mocks, sem Spring e sem Prometheus.
 */
public class FinalizaJogoComSMSMetricsTest {

    private RepositorioDeJogos daoMock;
    private GerenciadorSMS smsMock;
    private SimpleMeterRegistry registry;
    private FinalizaJogoComSMS finalizador;
    private Jogo jogo1;
    private Jogo jogo2;

    @BeforeEach
    public void setup() {
        daoMock = mock(RepositorioDeJogos.class);
        smsMock = mock(GerenciadorSMS.class);
        registry = new SimpleMeterRegistry();
        finalizador = new FinalizaJogoComSMS(daoMock, smsMock, registry);
        jogo1 = new Jogo("Jogo 1");
        jogo2 = new Jogo("Jogo 2");
    }

    private double contador(String nome) {
        return registry.get(nome).counter().count();
    }

    @Test
    public void deveContarJogosProcessadosESmsEnviados() {
        when(daoMock.emAndamento()).thenReturn(Arrays.asList(jogo1, jogo2));

        finalizador.finalizaENotifica();

        assertEquals(2.0, contador("jogos.processados"));
        assertEquals(2.0, contador("sms.enviados"));
        assertEquals(0.0, contador("jogos.falhas.salvar"));
        assertEquals(0.0, contador("sms.falhas"));
        assertEquals(1L, registry.get("jogos.finalizacao.tempo").timer().count());
    }

    @Test
    public void deveContarFalhaAoSalvarESemEnviarSms() {
        when(daoMock.emAndamento()).thenReturn(Arrays.asList(jogo1));
        doThrow(new RuntimeException("BD fora")).when(daoMock).atualiza(jogo1);

        assertThrows(RuntimeException.class, () -> finalizador.finalizaENotifica());

        assertEquals(1.0, contador("jogos.falhas.salvar"));
        assertEquals(0.0, contador("jogos.processados"));
        assertEquals(0.0, contador("sms.enviados"));
        verifyNoInteractions(smsMock);
        // o tempo e registrado mesmo quando ha erro
        assertEquals(1L, registry.get("jogos.finalizacao.tempo").timer().count());
    }

    @Test
    public void deveContarFalhaDeSmsDepoisDeSalvar() {
        when(daoMock.emAndamento()).thenReturn(Arrays.asList(jogo1));
        doThrow(new IllegalArgumentException("Jogo sem resultados")).when(smsMock).enviaParaVencedor(jogo1);

        assertThrows(IllegalArgumentException.class, () -> finalizador.finalizaENotifica());

        verify(daoMock).atualiza(jogo1);
        assertEquals(1.0, contador("jogos.processados"));
        assertEquals(1.0, contador("sms.falhas"));
        assertEquals(0.0, contador("sms.enviados"));
    }
}
