package ru.mentee.power.crm.spring.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EmailValidationClient {

  private final RestClient restClient;
  private final String baseUrl;

  public EmailValidationClient(
      RestClient restClient,
      @Value("${email.validation.base-url:http://localhost:8089}") String baseUrl) {
    this.restClient = restClient;
    this.baseUrl = baseUrl;
  }

  public EmailValidationResponse validateEmail(String email) {
    String url = baseUrl + "/api/validate/email?email=" + email;

    return restClient.get().uri(url).retrieve().body(EmailValidationResponse.class);
  }
}
