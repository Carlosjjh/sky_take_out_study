package com.sky.sky_server.simulator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SimulatorContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestTemplate simulatorRestTemplate;

    private MockRestServiceServer downstreamSimulator;

    @BeforeEach
    void setUp() {
        downstreamSimulator = MockRestServiceServer.bindTo(simulatorRestTemplate).build();
    }

    @Test
    void upstreamShouldReturnSameJsonAsDownstreamMock() throws Exception {
        String downstreamJson = StreamUtils.copyToString(
                new ClassPathResource("mock/downstream/employee-detail.json").getInputStream(),
                StandardCharsets.UTF_8);

        downstreamSimulator
                .expect(requestTo("http://localhost:9000/employees/1001"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(downstreamJson, MediaType.APPLICATION_JSON));

        MvcResult upstreamResult = mockMvc.perform(get("/simulator/employees/1001"))
                .andExpect(status().isOk())
                .andReturn();

        JSONAssert.assertEquals(downstreamJson, upstreamResult.getResponse().getContentAsString(), true);
        downstreamSimulator.verify();
    }
}
