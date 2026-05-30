package com.sky.sky_server.simulator.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.sky.sky_server.simulator.client.DownstreamClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simulator")
public class SimulatorController {

    private final DownstreamClient downstreamClient;

    public SimulatorController(DownstreamClient downstreamClient) {
        this.downstreamClient = downstreamClient;
    }

    @GetMapping("/employees/{id}")
    public JsonNode getEmployee(@PathVariable Long id) {
        return downstreamClient.getEmployee(id);
    }
}
