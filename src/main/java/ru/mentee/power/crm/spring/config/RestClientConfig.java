package ru.mentee.power.crm.spring.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Bean
  public RestClient restClient() {
    return RestClient.builder()
        .requestFactory(
            new org.springframework.http.client.SimpleClientHttpRequestFactory() {
              {
                setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
                setReadTimeout((int) Duration.ofSeconds(10).toMillis());
              }
            })
        .build();
  }
}
