package ru.mentee.power.crm.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.repository.LeadRepository;

@SpringBootTest
@Transactional
class LeadServiceIntegrationTest {

  @Autowired
  private LeadService leadService;

  @Autowired
  private LeadRepository leadRepository;

  @BeforeEach
  void setUp() {
    leadRepository.deleteAll();

    for (int i = 1; i <= 3; i++) {
      Lead lead = new Lead();
      lead.setFirstName("Lead" + i);
      lead.setEmail("lead" + i + "@test.com");
      lead.setPhone("+100000000" + i);
      lead.setCompany("Test Company");
      lead.setStatus("NEW");
      lead.setCreatedAt(LocalDateTime.now());
      leadRepository.save(lead);
    }


    for (int i = 1; i <= 2; i++) {
      Lead lead = new Lead();
      lead.setFirstName("Contacted" + i);
      lead.setEmail("contacted" + i + "@test.com");
      lead.setPhone("+200000000" + i);
      lead.setCompany("Test Company");
      lead.setStatus("CONTACTED");
      lead.setCreatedAt(LocalDateTime.now());
      leadRepository.save(lead);
    }
  }

  @Test
  void bulkUpdateStatus_shouldChangeAllLeadsWithOldStatus() {
    int updated = leadService.bulkUpdateStatus("NEW", "QUALIFIED");

    assertThat(updated).isEqualTo(3);

    long newCount = leadRepository.countByStatus("NEW");
    long qualifiedCount = leadRepository.countByStatus("QUALIFIED");

    assertThat(newCount).isEqualTo(0);
    assertThat(qualifiedCount).isEqualTo(3);
  }

  @Test
  void bulkDeleteByStatus_shouldRemoveAllLeadsWithGivenStatus() {
    int deleted = leadService.bulkDeleteByStatus("CONTACTED");

    assertThat(deleted).isEqualTo(2);

    long contactedCount = leadRepository.countByStatus("CONTACTED");
    assertThat(contactedCount).isEqualTo(0);

    long newCount = leadRepository.countByStatus("NEW");
    assertThat(newCount).isEqualTo(3);
  }

  @Test
  void bulkUpdateStatus_shouldReturnZero_whenNoLeadsWithOldStatus() {
    int updated = leadService.bulkUpdateStatus("WON", "LOST");

    assertThat(updated).isEqualTo(0);
  }
}