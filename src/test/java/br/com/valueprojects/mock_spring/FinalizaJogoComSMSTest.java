package br.com.valueprojects.mock_spring;

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import br.com.valueprojects.mock_spring.builder.CriadorDeJogo;
import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.service.FinalizaJogoComSMS;
import br.com.valueprojects.mock_spring.service.GerenciadorSMS;
import infra.JogoDao;

public class FinalizaJogoComSMSTest {
    
    private JogoDao daoMock;
    private GerenciadorSMS smsMock;
    private FinalizaJogoComSMS finalizador;
    
    @BeforeEach
    public void setup() {
        daoMock = mock(JogoDao.class);
        smsMock = mock(GerenciadorSMS.class);
        finalizador = new FinalizaJogoComSMS(daoMock, smsMock);
    }
    
    @Test
    public void deveVerificarOrdemDeSalvarEEnviarSMS() {
        Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);
        
        Jogo jogo = new CriadorDeJogo().para("Cata moedas")
            .naData(antiga).constroi();
        jogo.setId(1);
        jogo.finaliza();
        
        when(daoMock.emAndamento()).thenReturn(Arrays.asList(jogo));
        
        finalizador.finalizaENotifica();
        
        // Verificar ordem: DAO PRIMEIRO, depois SMS
        InOrder inOrder = inOrder(daoMock, smsMock);
        inOrder.verify(daoMock).atualiza(jogo);
        inOrder.verify(smsMock).enviaParaVencedor(jogo);
    }
    
    @Test
    public void naoDeveEnviarSMSSeFalharAoSalvar() {
        Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);
        
        Jogo jogo = new CriadorDeJogo().para("Cata moedas")
            .naData(antiga).constroi();
        jogo.setId(1);
        jogo.finaliza();
        
        when(daoMock.emAndamento()).thenReturn(Arrays.asList(jogo));
        doThrow(RuntimeException.class).when(daoMock).atualiza(jogo);
        
        try {
            finalizador.finalizaENotifica();
        } catch (RuntimeException e) {
            // Esperado
        }
        
        // SMS NÃO deve ter sido invocado
        verifyNoInteractions(smsMock);
    }
    
    @Test
    public void deveFinalizarComSucessoComUmJogo() {
        Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);
        
        Jogo jogo = new CriadorDeJogo().para("Derruba barreiras")
            .naData(antiga).constroi();
        jogo.setId(1);
        jogo.finaliza();
        
        when(daoMock.emAndamento()).thenReturn(Arrays.asList(jogo));
        
        finalizador.finalizaENotifica();
        
        verify(daoMock).atualiza(jogo);
        verify(smsMock).enviaParaVencedor(jogo);
    }
    
    @Test
    public void deveProcessarMultiplosJogos() {
        Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);
        
        Jogo jogo1 = new CriadorDeJogo().para("Jogo 1")
            .naData(antiga).constroi();
        jogo1.setId(1);
        jogo1.finaliza();
        
        Jogo jogo2 = new CriadorDeJogo().para("Jogo 2")
            .naData(antiga).constroi();
        jogo2.setId(2);
        jogo2.finaliza();
        
        when(daoMock.emAndamento()).thenReturn(Arrays.asList(jogo1, jogo2));
        
        finalizador.finalizaENotifica();
        
        // Verificar que ambos foram processados
        verify(daoMock, times(2)).atualiza(any());
        verify(smsMock, times(2)).enviaParaVencedor(any());
    }
    
    @Test
    public void deveVerificarQueSMSFoiChamadoComArgumentoCorreto() {
        Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);
        
        Jogo jogo = new CriadorDeJogo().para("Teste SMS")
            .naData(antiga).constroi();
        jogo.setId(1);
        jogo.finaliza();
        
        when(daoMock.emAndamento()).thenReturn(Arrays.asList(jogo));
        
        finalizador.finalizaENotifica();
        
        verify(smsMock).enviaParaVencedor(argThat(j -> 
            j.getDescricao().equals("Teste SMS")
        ));
    }
}
