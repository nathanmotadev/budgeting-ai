package dio.budgeting.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Relógio da aplicação. Injetar o Clock (em vez de chamar Instant.now() direto)
 * deixa os casos de uso testáveis com uma data fixa.
 */
@Configuration
public class ClockConfig {

    @Bean
    Clock clock(@Value("${budgeting.time-zone:America/Sao_Paulo}") String zone) {
        return Clock.system(ZoneId.of(zone));
    }
}
