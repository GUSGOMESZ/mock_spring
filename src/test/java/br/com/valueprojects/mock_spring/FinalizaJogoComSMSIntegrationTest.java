package br.com.valueprojects.mock_spring;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import br.com.valueprojects.mock_spring.builder.CriadorDeJogo;
import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.model.Participante;
import br.com.valueprojects.mock_spring.model.Resultado;
import br.com.valueprojects.mock_spring.service.FinalizaJogoComSMS;
import br.com.valueprojects.mock_spring.service.GerenciadorSMS;
import infra.JogoDao;

@SpringBootTest
@AutoConfigureMockMvc
public class FinalizaJogoComSMSIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private FinalizaJogoComSMS finalizaJogoComSMS;
    private JogoDao daoMock;
    private GerenciadorSMS smsMock;
    private Jogo jogoTeste;

    @BeforeEach
    public void setup() {
        daoMock = mock(JogoDao.class);
        smsMock = mock(GerenciadorSMS.class);
        finalizaJogoComSMS = new FinalizaJogoComSMS(daoMock, smsMock);
        
        Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);
        
        jogoTeste = new CriadorDeJogo()
                .para("Teste Integração")
                .naData(antiga)
                .constroi();
        jogoTeste.setId(1);
        jogoTeste.finaliza();
        
        Participante participante = new Participante(1, "Vencedor Teste");
        Resultado resultado = new Resultado(participante, 100.0);
        jogoTeste.anota(resultado);
    }

    @Test
    public void deveVerificarOrdemCriticaDAOAntesSMS() {
        when(daoMock.emAndamento()).thenReturn(java.util.Arrays.asList(jogoTeste));
        
        finalizaJogoComSMS.finalizaENotifica();
        
        InOrder inOrder = inOrder(daoMock, smsMock);
        inOrder.verify(daoMock).atualiza(jogoTeste);
        inOrder.verify(smsMock).enviaParaVencedor(jogoTeste);
    }

    @Test
    public void naoDeveEnviarSMSSeFalharAoSalvar() {
        when(daoMock.emAndamento()).thenReturn(java.util.Arrays.asList(jogoTeste));
        doThrow(new RuntimeException("Erro ao salvar no BD"))
                .when(daoMock).atualiza(jogoTeste);
        
        try {
            finalizaJogoComSMS.finalizaENotifica();
        } catch (RuntimeException e) {
            // Esperado
        }
        
        verifyNoInteractions(smsMock);
    }

    @Test
    public void deveProcessarUmJogoComSucesso() {
        when(daoMock.emAndamento()).thenReturn(java.util.Arrays.asList(jogoTeste));
        
        finalizaJogoComSMS.finalizaENotifica();
        
        verify(daoMock).atualiza(jogoTeste);
        verify(smsMock).enviaParaVencedor(jogoTeste);
    }
}
