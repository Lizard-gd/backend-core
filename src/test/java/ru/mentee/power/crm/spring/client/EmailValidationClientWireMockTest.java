package ru.mentee.power.crm.spring.client;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.ResourceAccessException;

@SpringBootTest
@WireMockTest(httpPort = 8089)
@TestPropertySource(properties = "email.validation.base-url=http://localhost:8089")
class EmailValidationClientWireMockTest {

  @Autowired private EmailValidationClient emailValidationClient;

  @Test
  void shouldReturnValid_whenEmailIsCorrect() {
    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .withQueryParam("email", equalTo("john@example.com"))
            .willReturn(
                okJson(
                    """
                        {
                            "email": "john@example.com",
                            "valid": true,
                            "reason": "Email exists"
                        }
                        """)));

    EmailValidationResponse response = emailValidationClient.validateEmail("john@example.com");

    assertThat(response).isNotNull();
    assertThat(response.valid()).isTrue();
    assertThat(response.email()).isEqualTo("john@example.com");
  }

  @Test
  void shouldReturnInvalid_whenEmailIsIncorrect() {
    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .withQueryParam("email", equalTo("invalid-email"))
            .willReturn(
                okJson(
                    """
                        {
                            "email": "invalid-email",
                            "valid": false,
                            "reason": "Invalid email format"
                        }
                        """)));

    EmailValidationResponse response = emailValidationClient.validateEmail("invalid-email");

    assertThat(response).isNotNull();
    assertThat(response.valid()).isFalse();
  }

  @Test
  void shouldHandleServerError_whenExternalServiceFails() {
    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .willReturn(serverError().withBody("Internal Server Error")));

    assertThatThrownBy(() -> emailValidationClient.validateEmail("any@example.com"))
        .isInstanceOf(org.springframework.web.client.HttpServerErrorException.class);
  }

  @Test
  void shouldHandleTimeout_whenExternalServiceIsSlow() {
    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .willReturn(okJson("{\"valid\": true}").withFixedDelay(15000)));

    assertThatThrownBy(() -> emailValidationClient.validateEmail("slow@example.com"))
        .isInstanceOf(ResourceAccessException.class);
  }
}
