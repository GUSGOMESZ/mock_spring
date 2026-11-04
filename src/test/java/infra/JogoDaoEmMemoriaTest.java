package infra;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.valueprojects.mock_spring.model.Jogo;

public class JogoDaoEmMemoriaTest {

    private JogoDaoEmMemoria dao;

    @BeforeEach
    public void setup() {
        dao = new JogoDaoEmMemoria(new Random(42));
    }

    @Test
    public void deveListarSomenteJogosEmAndamento() {
        Jogo emAndamento = new Jogo("Em andamento");
        Jogo finalizado = new Jogo("Finalizado");
        finalizado.finaliza();
        dao.salva(emAndamento);
        dao.salva(finalizado);

        assertEquals(1, dao.quantidadeEmAndamento());
        assertSame(emAndamento, dao.emAndamento().get(0));
    }

    @Test
    public void atualizaDeveFinalizarOJogo() {
        Jogo jogo = new Jogo("Jogo");
        dao.salva(jogo);

        dao.atualiza(jogo);

        assertTrue(jogo.isFinalizado());
        assertEquals(0, dao.quantidadeEmAndamento());
    }

    @Test
    public void deveFalharAoSalvarQuandoProbabilidadeForTotal() {
        Jogo jogo = new Jogo("Jogo");
        dao.setProbabilidadeFalha(1.0);

        assertThrows(IllegalStateException.class, () -> dao.atualiza(jogo));
        assertFalse(jogo.isFinalizado());
        assertEquals(1.0, dao.getProbabilidadeFalha());
    }

    @Test
    public void deveRejeitarProbabilidadeInvalida() {
        assertThrows(IllegalArgumentException.class, () -> dao.setProbabilidadeFalha(-0.1));
        assertThrows(IllegalArgumentException.class, () -> dao.setProbabilidadeFalha(1.1));
    }
}
