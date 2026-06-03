package ru.mentee.power.crm.service;

import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.repository.CompanyRepository;
import ru.mentee.power.crm.repository.DealRepository;
import ru.mentee.power.crm.repository.LeadRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest
@ActiveProfiles("test")
class LeadServiceRetryTest {

  @Autowired private LeadService leadService;

  @MockitoBean private LeadRepository leadRepository;

  @MockitoBean private CompanyRepository companyRepository;

  @MockitoBean private DealRepository dealRepository;

  private static WireMockRuntimeInfo wireMockRuntimeInfo;

  @BeforeAll
  static void captureWireMockRuntimeInfo(WireMockRuntimeInfo runtimeInfo) {
    wireMockRuntimeInfo = runtimeInfo;
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("email.validation.base-url", wireMockRuntimeInfo::getHttpBaseUrl);
    registry.add("resilience4j.retry.instances.email-validation.wait-duration", () -> "100ms");
  }

  @BeforeEach
  void setUp() {
    when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(leadRepository.findByEmailNative(any())).thenReturn(java.util.Optional.empty());
  }

  @Test
  void shouldRetryAndSucceed_whenFirstAttemptFails() {

    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .inScenario("Retry success scenario")
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(serverError())
            .willSetStateTo("First retry"));

    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .inScenario("Retry success scenario")
            .whenScenarioStateIs("First retry")
            .willReturn(serverError())
            .willSetStateTo("Second retry"));

    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .inScenario("Retry success scenario")
            .whenScenarioStateIs("Second retry")
            .willReturn(
                okJson(
                    """
                        {"email": "test@example.com", "valid": true, "reason": "OK"}
                        """)));

    Lead lead = leadService.addLead("RetryTest", "test@example.com", "+123", "NEW", null);
    assertThat(lead).isNotNull();
    assertThat(lead.getEmail()).isEqualTo("test@example.com");

    verify(3, getRequestedFor(urlPathEqualTo("/api/validate/email")));
  }

  @Test
  void shouldUseFallback_whenAllRetriesFail() {

    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .willReturn(serverError().withBody("Service Unavailable")));

    Lead lead = leadService.addLead("FallbackTest", "fallback@example.com", "+999", "NEW", null);
    assertThat(lead).isNotNull();
    assertThat(lead.getEmail()).isEqualTo("fallback@example.com");

    verify(3, getRequestedFor(urlPathEqualTo("/api/validate/email")));
  }

  @Test
  void shouldNotRetry_whenClientErrorOccurs() {

    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .willReturn(badRequest().withBody("{\"error\": \"Invalid email format\"}")));

    try {
      leadService.addLead("ClientError", "invalid", "+111", "NEW", null);
    } catch (Exception e) {
      assertThat(e).isInstanceOf(feign.FeignException.BadRequest.class);
    }

    verify(1, getRequestedFor(urlPathEqualTo("/api/validate/email")));
  }

  @Test
  void shouldRetry_whenTimeoutOccurs() {

    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .inScenario("Timeout retry")
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(ok().withFixedDelay(6000))
            .willSetStateTo("After timeout"));

    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .inScenario("Timeout retry")
            .whenScenarioStateIs("After timeout")
            .willReturn(
                okJson(
                    """
                        {"email": "timeout@example.com", "valid": true, "reason": "OK"}
                        """)));

    Lead lead = leadService.addLead("TimeoutTest", "timeout@example.com", "+222", "NEW", null);
    assertThat(lead).isNotNull();

    verify(2, getRequestedFor(urlPathEqualTo("/api/validate/email")));
  }
}
