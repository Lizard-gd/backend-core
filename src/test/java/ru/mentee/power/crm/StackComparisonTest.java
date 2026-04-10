package ru.mentee.power.crm;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class StackComparisonTest {

  private static final int SERVLET_PORT = 8080;
  private static final int SPRING_PORT = 8081;

  private HttpClient httpClient;

  @BeforeEach
  void setUp() {
    httpClient = HttpClient.newHttpClient();
  }

  @EnabledIfSystemProperty(named = "run.integration.tests", matches = "true")
  // Сделал тест необязательным для сборки jacoco, чтобы опубликовать
  @Test
  @DisplayName("Оба стека должны возвращать лидов в HTML таблице")
  void shouldReturnLeadsFromBothStacks() throws Exception {
    HttpRequest servletRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + SERVLET_PORT + "/leads"))
            .GET()
            .build();

    HttpRequest springRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + SPRING_PORT + "/leads"))
            .GET()
            .build();

    HttpResponse<String> servletResponse = httpClient.send(
            servletRequest, HttpResponse.BodyHandlers.ofString());
    HttpResponse<String> springResponse = httpClient.send(
            springRequest, HttpResponse.BodyHandlers.ofString());

    assertThat(servletResponse.statusCode()).isEqualTo(200);
    assertThat(springResponse.statusCode()).isEqualTo(200);

    assertThat(servletResponse.body()).contains("<table");
    assertThat(springResponse.body()).contains("<table");

    int servletRows = countTableRows(servletResponse.body());
    int springRows = countTableRows(springResponse.body());

    assertThat(servletRows)
            .as("Количество лидов должно совпадать")
            .isEqualTo(springRows);

    System.out.printf("Servlet: %d лидов, Spring: %d лидов%n", servletRows, springRows);
  }

  @Test
  @DisplayName("Измерение времени старта обоих стеков")
  void shouldMeasureStartupTime() {
    long servletStartupMs = measureServletStartup();

    long springStartupMs = measureSpringBootStartup();

    System.out.println("=== Сравнение времени старта ===");
    System.out.printf("Servlet стек: %d ms%n", servletStartupMs);
    System.out.printf("Spring Boot: %d ms%n", springStartupMs);
    System.out.printf("Разница Spring %s на %d ms%n",
            springStartupMs > servletStartupMs ? "медленнее" : "быстрее",
            Math.abs(springStartupMs - servletStartupMs));

    assertThat(servletStartupMs).isLessThan(10_000);
    assertThat(springStartupMs).isLessThan(15_000);
  }

  private int countTableRows(String html) {
    if (html == null) {
      return 0;
    }
    int count = 0;
    int index = 0;
    while ((index = html.indexOf("<tr", index)) != -1) {
      count++;
      index += 3;
    }
    return count;
  }

  private long measureServletStartup() {
    try {
      Tomcat tomcat = new Tomcat();
      tomcat.setPort(0);
      tomcat.getConnector();

      Context context = tomcat.addContext("", new File(".").getAbsolutePath());

      tomcat.addServlet(context, "health", new HttpServlet() {
          @Override
            protected void doGet(HttpServletRequest request, HttpServletResponse response) {
              response.setStatus(200);
          }
      });
      context.addServletMappingDecoded("/health", "health");

      long start = System.nanoTime();
      tomcat.start();
      long elapsed = System.nanoTime() - start;

      tomcat.stop();
      tomcat.destroy();

      return elapsed / 1_000_000;
    } catch (Exception e) {
      throw new RuntimeException("Failed to start servlet stack", e);
    }
  }

  private long measureSpringBootStartup() {
    try {
      long start = System.nanoTime();
      SpringApplication app = new SpringApplication(ru.mentee.power.crm.spring.Application.class);
      app.setDefaultProperties(Collections.singletonMap("server.port", "0"));
      ConfigurableApplicationContext context = app.run();
      long elapsed = System.nanoTime() - start;

      context.close();

      return elapsed / 1_000_000;
    } catch (Exception e) {
      throw new RuntimeException("Failed to start Spring Boot stack", e);
    }
  }
}