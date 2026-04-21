package ru.mentee.power.crm.spring.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DemoControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void demoEndpointReturnsAllInjections() throws Exception {
    mockMvc.perform(get("/demo"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("✓ Injected")))
                .andExpect(content()
                        .string(org.hamcrest.Matchers.containsString("Recommendation")));
  }
}