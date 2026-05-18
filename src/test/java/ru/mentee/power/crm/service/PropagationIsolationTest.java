package ru.mentee.power.crm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.power.crm.model.Company;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.repository.CompanyRepository;
import ru.mentee.power.crm.repository.LeadRepository;

@SpringBootTest
class PropagationIsolationTest {

  @Autowired
  private ParentService parentService;

  @Autowired
  private ChildService childService;

  @Autowired
  private LeadRepository leadRepository;

  @Autowired
  private CompanyRepository companyRepository;

  private UUID testLeadId;

  @BeforeEach
  void setUp() {
    leadRepository.deleteAll();
    companyRepository.deleteAll();

    Company company = new Company();
    company.setName("Propagation Corp");
    company.setIndustry("Testing");
    company = companyRepository.save(company);

    Lead lead = new Lead();
    lead.setFirstName("PropTest");
    lead.setEmail("prop@test.com");
    lead.setPhone("+500000000");
    lead.setCompany(company);
    lead.setStatus("INITIAL");
    lead.setCreatedAt(LocalDateTime.now());
    lead = leadRepository.save(lead);
    testLeadId = lead.getId();
  }

  @Test
  void propagation_REQUIRED_shouldReuseTransaction() {
    assertThatThrownBy(() -> parentService.parentMethodWithRequired(testLeadId, true))
            .isInstanceOf(RuntimeException.class);

    Lead lead = leadRepository.findById(testLeadId).orElseThrow();
    assertThat(lead.getStatus()).isEqualTo("INITIAL");
  }

  @Test
  void propagation_REQUIRES_NEW_shouldCreateNewTransaction() {
    Lead lead = leadRepository.findById(testLeadId).orElseThrow();
    lead.setStatus("BEFORE_REQUIRES_NEW");
    leadRepository.save(lead);

    childService.requiresNewMethod(testLeadId, false);

    Lead updated = leadRepository.findById(testLeadId).orElseThrow();
    assertThat(updated.getStatus()).isEqualTo("CHILD_REQUIRES_NEW");
  }

  @Test
  @Transactional
  void propagation_REQUIRES_NEW_shouldIsolateFromParentRollback() {
    Lead lead = leadRepository.findById(testLeadId).orElseThrow();
    lead.setStatus("PARENT_CHANGE");
    leadRepository.save(lead);

    assertThatThrownBy(() -> childService.requiresNewMethod(testLeadId, true))
            .isInstanceOf(RuntimeException.class);

    Lead leadAfter = leadRepository.findById(testLeadId).orElseThrow();
    assertThat(leadAfter.getStatus()).isEqualTo("PARENT_CHANGE");
  }

  @Test
  void propagation_MANDATORY_requiresExistingTransaction() {
    assertThatThrownBy(() -> childService.mandatoryMethod(testLeadId))
            .isInstanceOf(Exception.class);
  }

  @Test
  void isolation_READ_COMMITTED_allowsNonRepeatableRead() throws Exception {
    Lead lead = leadRepository.findById(testLeadId).orElseThrow();
    lead.setStatus("V1");
    leadRepository.save(lead);

    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
      Lead l = leadRepository.findById(testLeadId).orElseThrow();
      l.setStatus("V2");
      leadRepository.save(l);
    });
    future.join();

    Lead afterB = leadRepository.findById(testLeadId).orElseThrow();
    assertThat(afterB.getStatus()).isEqualTo("V2");
  }
}