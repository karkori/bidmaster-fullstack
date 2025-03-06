package dev.mostapha.bidmaster.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.dialect.PostgresDialect;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/**
 * Configuración de R2DBC para proporcionar los beans necesarios
 * y habilitar los repositorios R2DBC
 */
@Configuration
@EnableR2dbcRepositories(basePackages = "dev.mostapha.bidmaster.adapter.out.persistence.repository")
public class R2dbcConfig extends AbstractR2dbcConfiguration {

    private final ConnectionFactory connectionFactory;

    public R2dbcConfig(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public ConnectionFactory connectionFactory() {
        return connectionFactory;
    }
    
    @Bean
    public R2dbcDialect r2dbcDialect() {
        return new PostgresDialect();
    }
    
    @Bean
    @Override
    public R2dbcCustomConversions r2dbcCustomConversions() {
        return super.r2dbcCustomConversions();
    }

    // No necesitamos definir manualmente el r2dbcEntityTemplate
    // ya que AbstractR2dbcConfiguration lo proporciona automáticamente
}
