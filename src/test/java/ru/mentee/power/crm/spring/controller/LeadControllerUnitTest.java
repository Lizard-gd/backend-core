package ru.mentee.power.crm.spring.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import ru.mentee.power.crm.spring.MockLeadService;

public class LeadControllerUnitTest {

  @Test
  void shouldCreateControllerWithoutSpring() {
    MockLeadService mockService = new MockLeadService();

    LeadController controller = new LeadController(mockService);

    String response = controller.home();
    assertThat(response).contains("2 leads");
  }

  @Test
  void shouldUseInjectedService() {
    MockLeadService mockService = new MockLeadService();
    LeadController controller = new LeadController(mockService);

    String response = controller.home();

    assertThat(response).isNotNull();
    assertThat(response).contains("Spring Boot CRM is running");
  }
}
