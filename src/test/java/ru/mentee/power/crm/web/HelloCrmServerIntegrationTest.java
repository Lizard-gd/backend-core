package ru.mentee.power.crm.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HelloCrmServerIntegrationTest {

  private HelloCrmServer server;
  private HttpClient client;

  @BeforeEach
  void setUp() throws Exception {
    server = new HelloCrmServer(8081); // используем другой порт для тестов
    server.start();
    client = HttpClient.newHttpClient();
            // даем серверу время на запуск
    Thread.sleep(100);
  }

  @AfterEach
  void tearDown() {
    server.stop();
  }

  @Test
  void testGetHelloReturnsHtml() throws Exception {
            // Given
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:8081/hello"))
        .GET()
        .build();

            // When
    HttpResponse<String> response = client.send(request,
        HttpResponse.BodyHandlers.ofString());

            // Then
    assertEquals(200, response.statusCode());
    assertTrue(response.headers().firstValue("Content-Type")
        .orElse("").contains("text/html"));
    assertTrue(response.body().contains("Hello CRM!"));
  }

  @Test
  void testGetUnknownPathReturns404() throws Exception {
            // Given
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:8081/unknown"))
        .GET()
        .build();

            // When
    HttpResponse<String> response = client.send(request,
        HttpResponse.BodyHandlers.ofString());
            // Then
    assertEquals(404, response.statusCode());
  }
}
