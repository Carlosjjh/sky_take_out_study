package com.sky.sky_server.simulator.client;

import com.fasterxml.jackson.databind.JsonNode;

public interface DownstreamClient {

    JsonNode getEmployee(Long id);
}
