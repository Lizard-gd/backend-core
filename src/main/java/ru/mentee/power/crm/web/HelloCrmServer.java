package ru.mentee.power.crm.web;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HelloCrmServer {
  private final HttpServer server;

  public HelloCrmServer(int port) throws IOException {
    this.server = HttpServer.create(new InetSocketAddress(port), 0);
  }

  public void start() {
    server.createContext("/hello", new HelloHandler());
    server.start();
    System.out.println("Server started on http://localhost:" + getPort());
  }

  private int getPort() {
    return server.getAddress().getPort();
  }

  public void stop() {
    server.stop(0);
  }

  static class HelloHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      System.out.println("Handler called!");

      String html = "<!DOCTYPE html>\n"
          + "<html>\n"
          + "<head>\n"
          + "    <meta charset=\"UTF-8\">\n"
          + "    <title>Hello CRM</title>\n"
          + "</head>\n"
          + "<body>\n"
          + "    <h1>Hello CRM!</h1>\n"
          + "</body>\n"
          + "</html>";

      exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
      byte[] responseBytes = html.getBytes(StandardCharsets.UTF_8);

      exchange.sendResponseHeaders(200, responseBytes.length);
      OutputStream os = exchange.getResponseBody();
      os.write(responseBytes);
      os.close();
    }
  }
}
