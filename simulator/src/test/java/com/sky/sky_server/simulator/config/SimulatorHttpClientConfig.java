package com.sky.sky_server.simulator.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SimulatorHttpClientConfig {

    @Bean
    public RestTemplate simulatorRestTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
