package com.grupok.publicaciones;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    @Bean
    public DataSource dataSource() {
        String rawUrl = System.getenv("DATABASE_URL");
        String jdbcUrl = rawUrl != null
            ? "jdbc:" + rawUrl.trim()
            : "jdbc:postgresql://postgres:postgres@localhost:5432/foro_publicaciones";
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        return new HikariDataSource(config);
    }
}
