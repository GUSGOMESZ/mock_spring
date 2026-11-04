package infra;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.com.valueprojects.mock_spring.model.Jogo;

/**
 * Repositorio em memoria usado SOMENTE para demonstrar a observabilidade
 * com a aplicacao rodando (o JogoDao real depende de MySQL).
 *
 * Permite simular falhas de gravacao para que as metricas de erro
 * apareçam no Prometheus/Grafana.
 */
public class JogoDaoEmMemoria implements RepositorioDeJogos {

    private final List<Jogo> jogos = new ArrayList<>();
    private final Random random;
    private double probabilidadeFalha = 0.0;

    public JogoDaoEmMemoria(Random random) {
        this.random = random;
    }

    public synchronized void salva(Jogo jogo) {
        jogos.add(jogo);
    }

    @Override
    public synchronized List<Jogo> emAndamento() {
        List<Jogo> selecionados = new ArrayList<>();
        for (Jogo jogo : jogos) {
            if (!jogo.isFinalizado()) selecionados.add(jogo);
        }
        return selecionados;
    }

    @Override
    public synchronized void atualiza(Jogo jogo) {
        if (random.nextDouble() < probabilidadeFalha) {
            throw new IllegalStateException("Falha simulada ao salvar o jogo: " + jogo.getDescricao());
        }
        jogo.finaliza();
    }

    public synchronized int quantidadeEmAndamento() {
        return emAndamento().size();
    }

    public void setProbabilidadeFalha(double probabilidadeFalha) {
        if (probabilidadeFalha < 0.0 || probabilidadeFalha > 1.0) {
            throw new IllegalArgumentException("Probabilidade deve estar entre 0 e 1");
        }
        this.probabilidadeFalha = probabilidadeFalha;
    }

    public double getProbabilidadeFalha() {
        return probabilidadeFalha;
    }
}
