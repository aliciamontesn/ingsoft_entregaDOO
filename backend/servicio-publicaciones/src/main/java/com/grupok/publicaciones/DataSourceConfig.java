package com.grupok.publicaciones;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DataSourceConfig {
    @Bean
    public DataSource dataSource() {
        String rawUrl = System.getenv("DATABASE_URL");
        HikariConfig config = new HikariConfig();

        if (rawUrl != null) {
            URI uri;
            try {
                uri = new URI(rawUrl.trim());
            } catch (Exception e) {
                throw new RuntimeException("Invalid DATABASE_URL: " + e.getMessage(), e);
            }
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String database = uri.getPath().replaceFirst("^/", "");
            config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + database);
            String userInfo = uri.getUserInfo();
            if (userInfo != null) {
                int colon = userInfo.indexOf(':');
                if (colon >= 0) {
                    config.setUsername(userInfo.substring(0, colon));
                    config.setPassword(userInfo.substring(colon + 1));
                } else {
                    config.setUsername(userInfo);
                }
            }
        } else {
            config.setJdbcUrl("jdbc:postgresql://localhost:5432/foro_publicaciones");
            config.setUsername("postgres");
            config.setPassword("postgres");
        }

        return new HikariDataSource(config);
    }
}
