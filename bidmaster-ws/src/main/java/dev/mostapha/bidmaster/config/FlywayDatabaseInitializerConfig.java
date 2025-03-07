package dev.mostapha.bidmaster.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration()
@Profile("dev") // Solo para desarrollo
public class FlywayDatabaseInitializerConfig{

    @Bean
    public ApplicationRunner cleanAndMigrateDatabase(Flyway flyway) {
        return args -> {
            flyway.clean();
            flyway.migrate();
        };
    }
}
