package ru.mentee.power.crm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.repository.LeadRepository;

@SpringBootTest
class SelfInvocationProblemTest {

  @Autowired
  private LeadService leadService;

  @Autowired
  private LeadRepository leadRepository;

  @Test
  void demonstrateSelfInvocationProblem() {
    List<UUID> ids = new ArrayList<>();
    try {

      for (int i = 1; i <= 3; i++) {
        Lead lead = new Lead();
        lead.setFirstName("SelfInvoke" + i);
        lead.setEmail("self" + i + "@test.com");
        lead.setPhone("+30000000" + i);
        lead.setCompany("SelfTest");
        lead.setStatus("NEW");
        lead.setCreatedAt(LocalDateTime.now());
        leadRepository.save(lead);
        ids.add(lead.getId());
      }

      assertThatThrownBy(() -> leadService.processLeads(ids))
              .isInstanceOf(RuntimeException.class);

      Lead firstLead = leadRepository.findById(ids.get(0)).orElseThrow();
      assertThat(firstLead.getStatus()).isEqualTo("PROCESSED");

      Lead secondLead = leadRepository.findById(ids.get(1)).orElseThrow();
      assertThat(secondLead.getStatus()).isEqualTo("NEW");

      Lead thirdLead = leadRepository.findById(ids.get(2)).orElseThrow();
      assertThat(thirdLead.getStatus()).isEqualTo("NEW");
    } finally {
      leadRepository.deleteAllById(ids);
    }
  }
}