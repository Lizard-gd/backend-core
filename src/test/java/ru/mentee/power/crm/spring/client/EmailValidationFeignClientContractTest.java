package ru.mentee.power.crm.spring.client;

import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest
@ActiveProfiles("test")
class EmailValidationFeignClientContractTest {

  @Autowired private EmailValidationFeignClient feignClient;

  private static WireMockRuntimeInfo wireMockRuntimeInfo;

  @BeforeAll
  static void captureWireMockRuntimeInfo(WireMockRuntimeInfo runtimeInfo) {
    wireMockRuntimeInfo = runtimeInfo;
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("email.validation.base-url", wireMockRuntimeInfo::getHttpBaseUrl);
  }

  @Test
  void shouldReturnValidResponse_whenEmailIsValid() {
    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .withQueryParam("email", equalTo("john@example.com"))
            .willReturn(
                okJson(
                    """
                        {
                            "email": "john@example.com",
                            "valid": true,
                            "reason": "Email is valid"
                        }
                        """)));

    EmailValidationResponse response = feignClient.validateEmail("john@example.com");
    assertThat(response.valid()).isTrue();
    assertThat(response.email()).isEqualTo("john@example.com");
    verify(
        getRequestedFor(urlPathEqualTo("/api/validate/email"))
            .withQueryParam("email", equalTo("john@example.com")));
  }

  @Test
  void shouldReturnInvalidResponse_whenEmailIsInvalid() {
    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .withQueryParam("email", equalTo("invalid@bad.com"))
            .willReturn(
                okJson(
                    """
                        {
                            "email": "invalid@bad.com",
                            "valid": false,
                            "reason": "Domain does not accept emails"
                        }
                        """)));

    EmailValidationResponse response = feignClient.validateEmail("invalid@bad.com");
    assertThat(response.valid()).isFalse();
    assertThat(response.reason()).contains("Domain does not accept emails");
    verify(
        getRequestedFor(urlPathEqualTo("/api/validate/email"))
            .withQueryParam("email", equalTo("invalid@bad.com")));
  }

  @Test
  void shouldThrowFeignException_whenExternalServiceReturns500() {
    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .willReturn(serverError().withBody("Internal Server Error")));

    assertThatThrownBy(() -> feignClient.validateEmail("any@example.com"))
        .isInstanceOf(feign.FeignException.class);
  }

  @Test
  void shouldThrowBadRequestException_whenExternalServiceReturns400() {
    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .willReturn(badRequest().withBody("{\"error\": \"Invalid email format\"}")));

    assertThatThrownBy(() -> feignClient.validateEmail("not-an-email"))
        .isInstanceOf(feign.FeignException.BadRequest.class);
  }
}
