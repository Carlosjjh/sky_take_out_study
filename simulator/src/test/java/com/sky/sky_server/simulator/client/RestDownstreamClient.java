package com.sky.sky_server.simulator.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestDownstreamClient implements DownstreamClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public RestDownstreamClient(
            RestTemplate restTemplate,
            @Value("${simulator.downstream.base-url:http://localhost:9000}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public JsonNode getEmployee(Long id) {
        return restTemplate.getForObject(baseUrl + "/employees/{id}", JsonNode.class, id);
    }
}
