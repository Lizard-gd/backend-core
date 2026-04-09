package ru.mentee.power.crm;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MainTest {

  private Thread tomcatThread;
  private Exception startupException;

  @BeforeEach
  void setUp() throws Exception {
    startupException = null;
    tomcatThread = new Thread(() -> {
      try {
        Main.main(new String[]{});
      } catch (Exception e) {
        startupException = e;
      }
    });
    tomcatThread.start();

    Thread.sleep(3000);
  }

  @AfterEach
  void tearDown() {
    if (tomcatThread != null && tomcatThread.isAlive()) {
      tomcatThread.interrupt();
    }
  }

  @Test
  void shouldStartTomcatAndReturnLeadsPage() throws Exception {

    assertThat(startupException).isNull();


    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/leads"))
                .GET()
                .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("CRM System");
    assertThat(response.body()).contains("Lead List");
    assertThat(response.body()).contains("john@example.com");
    assertThat(response.body()).contains("Tech Corp");
  }

  @Test
  void shouldReturnHtmlContentType() throws Exception {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/leads"))
                .GET()
                .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    String contentType = response.headers().firstValue("Content-Type").orElse("");
    assertThat(contentType).contains("text/html");
    assertThat(contentType).contains("charset=UTF-8");
  }

  @Test
  void shouldReturnXssProtectedContent() throws Exception {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/leads"))
                .GET()
                .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


    assertThat(response.body()).contains("&lt;script&gt;");
    assertThat(response.body()).doesNotContain("<script>");
  }
}