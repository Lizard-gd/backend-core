package ru.mentee.power.crm.spring.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import ru.mentee.power.crm.service.LeadService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LeadControllerIntegrationTest {

  @LocalServerPort
  private int port;

  @Autowired
  private LeadService leadService;

  @BeforeEach
  void setUp() {
    if (leadService.findAll().isEmpty()) {
      leadService.addLead("john@example.com", "+123456789", "Tech Corp", "NEW");
    }
  }

  @Test
  void shouldReturn200Ok_whenGetLeads() throws Exception {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/leads"))
                .GET()
                .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("Lead List");
    assertThat(response.body()).contains("john@example.com");
  }
}