package ru.mentee.power.crm;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.mentee.power.crm.repository.LeadRepository;
import ru.mentee.power.crm.service.LeadService;
import ru.mentee.power.crm.servlet.LeadListServlet;

public class StackComparisonTest {

  private static final int SERVLET_PORT = 8080;
  private static final int SPRING_PORT = 8081;

  private HttpClient httpClient;
  private Tomcat servletTomcat;
  private ConfigurableApplicationContext springContext;

  @BeforeEach
  void setUp() {
    httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();
  }

  @AfterEach
  void tearDown() throws Exception {
    // Остановка Spring Boot
    if (springContext != null && springContext.isActive()) {
      springContext.close();
    }

    // Остановка Tomcat
    if (servletTomcat != null) {
      servletTomcat.stop();
      servletTomcat.destroy();
    }
  }

  @Test
  @DisplayName("Оба стека должны возвращать лидов в HTML таблице")
  void shouldReturnLeadsFromBothStacks() throws Exception {
    // Запуск обоих приложений
    startServletStack();
    startSpringStack();

    // Ожидание готовности приложений
    waitForApplication(SERVLET_PORT);
    waitForApplication(SPRING_PORT);

    HttpRequest servletRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + SERVLET_PORT + "/leads"))
            .timeout(Duration.ofSeconds(5))
            .GET()
            .build();

    HttpRequest springRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + SPRING_PORT + "/leads"))
            .timeout(Duration.ofSeconds(5))
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
            .as("Servlet стек должен вернуть хотя бы 1 лид")
            .isGreaterThan(0);

    assertThat(springRows)
            .as("Spring стек должен вернуть хотя бы 1 лид")
            .isGreaterThan(0);

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

  private void startServletStack() throws Exception {
    servletTomcat = new Tomcat();
    servletTomcat.setPort(SERVLET_PORT);

    LeadRepository repository = new LeadRepository();
    LeadService leadService = new LeadService(repository);

    // Добавляем тестовые данные
    leadService.addLead("test1@example.com", "+111111111", "Company 1", "NEW");
    leadService.addLead("test2@example.com", "+222222222", "Company 2", "QUALIFIED");
    leadService.addLead("test3@example.com", "+333333333", "Company 3", "NEW");

    Context context = servletTomcat.addContext("", new File(".").getAbsolutePath());
    context.getServletContext().setAttribute("leadService", leadService);

    servletTomcat.addServlet(context, "LeadListServlet", new LeadListServlet());
    context.addServletMappingDecoded("/leads", "LeadListServlet");

    servletTomcat.getConnector();
    servletTomcat.start();
    System.out.println("Servlet Tomcat started on port " + SERVLET_PORT);
  }

  private void startSpringStack() throws Exception {
    SpringApplication app = new SpringApplication(ru.mentee.power.crm.spring.Application.class);
    app.setDefaultProperties(Collections.singletonMap("server.port", String.valueOf(SPRING_PORT)));
    // Отключаем автоконфигурацию, чтобы не мешала тесту
    springContext = app.run();
    System.out.println("Spring Boot started on port " + SPRING_PORT);
  }

  private void waitForApplication(int port) throws Exception {
    int maxRetries = 20;
    int retryDelayMs = 250;

    for (int i = 0; i < maxRetries; i++) {
      try {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/leads"))
                .timeout(Duration.ofSeconds(2))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
          System.out.println("Application on port " + port + " is ready");
          return;
        }
      } catch (Exception e) {
        // Игнорируем и повторяем
      }
      Thread.sleep(retryDelayMs);
    }
    throw new RuntimeException("Application on port " + port + " did not become ready in time");
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
    ConfigurableApplicationContext tempContext = null;
    try {
      long start = System.nanoTime();
      SpringApplication app = new SpringApplication(ru.mentee.power.crm.spring.Application.class);
      app.setDefaultProperties(Collections.singletonMap("server.port", "0"));
      tempContext = app.run();
      long elapsed = System.nanoTime() - start;

      tempContext.close();

      return elapsed / 1_000_000;
    } catch (Exception e) {
      if (tempContext != null) {
        tempContext.close();
      }
      throw new RuntimeException("Failed to start Spring Boot stack", e);
    }
  }
}