package br.com.valueprojects.mock_spring.service;

import java.util.List;

import br.com.valueprojects.mock_spring.model.Jogo;
import infra.RepositorioDeJogos;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class FinalizaJogoComSMS {

    private final RepositorioDeJogos dao;
    private final GerenciadorSMS sms;

    // Pratica 4: metricas de negocio expostas ao Prometheus
    private final Counter jogosProcessados;
    private final Counter falhasAoSalvar;
    private final Counter smsEnviados;
    private final Counter falhasSms;
    private final Timer tempoFinalizacao;

    /** Construtor usado nos testes das Praticas 2 e 3 (metricas ficam em memoria). */
    public FinalizaJogoComSMS(RepositorioDeJogos dao, GerenciadorSMS sms) {
        this(dao, sms, new SimpleMeterRegistry());
    }

    /** Construtor usado pela aplicacao: registra as metricas no registry do Spring (Prometheus). */
    public FinalizaJogoComSMS(RepositorioDeJogos dao, GerenciadorSMS sms, MeterRegistry registry) {
        this.dao = dao;
        this.sms = sms;
        this.jogosProcessados = Counter.builder("jogos.processados")
                .description("Jogos finalizados e salvos na base")
                .register(registry);
        this.falhasAoSalvar = Counter.builder("jogos.falhas.salvar")
                .description("Falhas ao salvar jogo finalizado (SMS nao e enviado)")
                .register(registry);
        this.smsEnviados = Counter.builder("sms.enviados")
                .description("SMS enviados aos vencedores")
                .register(registry);
        this.falhasSms = Counter.builder("sms.falhas")
                .description("Falhas no envio de SMS")
                .register(registry);
        this.tempoFinalizacao = Timer.builder("jogos.finalizacao.tempo")
                .description("Tempo de execucao de finalizaENotifica()")
                .publishPercentileHistogram()
                .register(registry);
    }

    public void finalizaENotifica() {
        tempoFinalizacao.record(this::processa);
    }

    private void processa() {
        List<Jogo> jogos = dao.emAndamento();

        for (Jogo jogo : jogos) {
            // PASSO 1: Salvar na base (SEMPRE PRIMEIRO)
            try {
                dao.atualiza(jogo);
            } catch (RuntimeException e) {
                falhasAoSalvar.increment();
                throw e; // regra de negocio: sem salvar, sem SMS
            }
            jogosProcessados.increment();

            // PASSO 2: Enviar SMS (DEPOIS)
            try {
                sms.enviaParaVencedor(jogo);
            } catch (RuntimeException e) {
                falhasSms.increment();
                throw e;
            }
            smsEnviados.increment();
        }
    }
}
