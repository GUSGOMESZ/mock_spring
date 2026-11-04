package infra;

import java.util.List;

import br.com.valueprojects.mock_spring.model.Jogo;

/**
 * Contrato de persistencia usado por FinalizaJogoComSMS.
 *
 * Com esta interface o servico deixa de depender da classe concreta JogoDao
 * (que exige MySQL). Em producao usa-se JogoDao; na demonstracao de
 * observabilidade usa-se JogoDaoEmMemoria; nos testes, um mock do Mockito.
 * E a Injecao de Dependencia (Pratica 1, Q5) aplicada na pratica.
 */
public interface RepositorioDeJogos {

    List<Jogo> emAndamento();

    void atualiza(Jogo jogo);
}
