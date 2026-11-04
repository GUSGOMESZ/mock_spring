package br.com.valueprojects.mock_spring.config;

import java.util.Random;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.valueprojects.mock_spring.service.FinalizaJogoComSMS;
import br.com.valueprojects.mock_spring.service.GerenciadorSMS;
import infra.JogoDaoEmMemoria;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Pratica 4: monta os beans usados na demonstracao de observabilidade.
 * O MeterRegistry e criado pelo Actuator e publicado em /actuator/prometheus.
 */
@Configuration
public class ObservabilidadeConfig {

    @Bean
    public JogoDaoEmMemoria jogoDaoEmMemoria(MeterRegistry registry) {
        JogoDaoEmMemoria dao = new JogoDaoEmMemoria(new Random());
        Gauge.builder("jogos.em.andamento", dao, JogoDaoEmMemoria::quantidadeEmAndamento)
                .description("Jogos aguardando finalizacao")
                .register(registry);
        return dao;
    }

    @Bean
    public GerenciadorSMS gerenciadorSMS() {
        return new GerenciadorSMS();
    }

    @Bean
    public FinalizaJogoComSMS finalizaJogoComSMS(JogoDaoEmMemoria dao, GerenciadorSMS sms, MeterRegistry registry) {
        return new FinalizaJogoComSMS(dao, sms, registry);
    }
}
