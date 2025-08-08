package com.marcicomentariosfacebook.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import org.springframework.core.env.Environment;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class JdbcConfig {

    private final Environment env;

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(env.getProperty("spring.datasource.url"))
                .username(env.getProperty("spring.datasource.username"))
                .password(env.getProperty("spring.datasource.password"))
                .driverClassName(env.getProperty("spring.datasource.driver-class-name"))
                .build();
    }
}