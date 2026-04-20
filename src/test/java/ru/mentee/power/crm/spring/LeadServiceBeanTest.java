package ru.mentee.power.crm.spring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import ru.mentee.power.crm.repository.LeadRepository;
import ru.mentee.power.crm.service.LeadService;

@SpringBootTest
public class LeadServiceBeanTest {
  @Autowired
  private ApplicationContext context;

  @Test
  void shouldCreateLeadServiceBean() {
    LeadService service = context.getBean(LeadService.class);
    assertThat(service).isNotNull();
  }

  @Test
  void shouldCreateLeadRepositoryBean() {
    LeadRepository repo = context.getBean(LeadRepository.class);
    assertThat(repo).isNotNull();
  }

  @Test
  void shouldInjectLeadRepositoryIntoService() {
    LeadService service = context.getBean(LeadService.class);
    assertThat(service.findAll()).isEmpty();
  }
}
