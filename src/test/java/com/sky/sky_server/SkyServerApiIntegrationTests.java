package com.sky.sky_server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SkyServerApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void healthEndpointShouldBePublic() throws Exception {
        mockMvc.perform(get("/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("sky-server is running"));
    }

    @Test
    void adminWebShouldBeServedFromTheApplicationRoot() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));
    }

    @Test
    void adminShouldLoginThenManageCategories() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(post("/admin/category")
                .header("token", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Hot dishes\",\"type\":1,\"sort\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(get("/admin/category/page")
                .header("token", token)
                .param("page", "1")
                .param("pageSize", "10")
                .param("name", "Hot dishes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].name").value("Hot dishes"));
    }

    @Test
    void adminShouldPublishDishThenCustomerCanSubmitOrder() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(post("/admin/category")
                .header("token", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Customer dishes\",\"type\":1,\"sort\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(1));

        MvcResult categoryResult = mockMvc.perform(get("/admin/category/page")
                .header("token", token)
                .param("page", "1")
                .param("pageSize", "10")
                .param("name", "Customer dishes"))
                .andExpect(status().isOk())
                .andReturn();
        long categoryId = objectMapper.readTree(categoryResult.getResponse().getContentAsString())
                .path("data").path("records").get(0).path("id").asLong();

        MvcResult dishResult = mockMvc.perform(post("/admin/dish")
                .header("token", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Customer noodles\",\"categoryId\":" + categoryId
                        + ",\"price\":18.50,\"description\":\"test dish\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();
        long dishId = objectMapper.readTree(dishResult.getResponse().getContentAsString())
                .path("data").asLong();

        mockMvc.perform(get("/user/category/list").param("type", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(get("/user/dish/list").param("categoryId", String.valueOf(categoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Customer noodles"));

        mockMvc.perform(post("/user/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerName\":\"Tester\",\"phone\":\"13800138000\",\"address\":\"Test road 1\",\"items\":[{\"dishId\":"
                        + dishId + ",\"number\":2}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    void adminEndpointShouldRejectRequestsWithoutToken() throws Exception {
        mockMvc.perform(get("/admin/employee/page").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isUnauthorized());
    }

    private String loginAsAdmin() throws Exception {
        MvcResult result = mockMvc.perform(post("/admin/employee/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = body.path("data").path("token").asText();
        assertThat(token).isNotBlank();
        return token;
    }
}
