package ru.mentee.power.crm.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HelloCRMServerTest {

  private HelloCrmServer server;
  private HttpClient client;

  @BeforeEach
  void setUp() throws Exception {
    server = new HelloCrmServer(8081);
    server.start();
    client = HttpClient.newHttpClient();
    Thread.sleep(500); // Даем время на запуск
  }

  @AfterEach
  void tearDown() {
    server.stop();
  }

  @Test
  void testGetHelloReturns200AndHtml() throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:8081/hello"))
        .GET()
        .build();

    HttpResponse<String> response = client.send(request,
        HttpResponse.BodyHandlers.ofString());

    assertEquals(200, response.statusCode());
    assertTrue(response.headers().firstValue("Content-Type")
        .orElse("").contains("text/html"));
    assertTrue(response.body().contains("Hello CRM!"));
    assertTrue(response.body().contains("<!DOCTYPE html>"));
  }

  @Test
  void testGetHelloReturnsCorrectHeaders() throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:8081/hello"))
        .GET()
        .build();

    HttpResponse<String> response = client.send(request,
        HttpResponse.BodyHandlers.ofString());

    String contentType = response.headers().firstValue("Content-Type").orElse("");
    assertTrue(contentType.contains("text/html"));
    assertTrue(contentType.contains("charset=UTF-8"));
  }

  @Test
  void testGetUnknownPathReturns404() throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:8081/unknown"))
        .GET()
        .build();

    HttpResponse<String> response = client.send(request,
        HttpResponse.BodyHandlers.ofString());

    assertEquals(404, response.statusCode());
  }

  @Test
  void testGetHelloReturnsNonEmptyBody() throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:8081/hello"))
        .GET()
        .build();

    HttpResponse<String> response = client.send(request,
        HttpResponse.BodyHandlers.ofString());

    assertNotNull(response.body());
    assertTrue(response.body().length() > 0);
  }
}
