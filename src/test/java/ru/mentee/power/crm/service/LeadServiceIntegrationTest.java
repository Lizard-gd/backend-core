package ru.mentee.power.crm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.power.crm.dto.CreateDealRequest;
import ru.mentee.power.crm.model.Deal;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.repository.DealRepository;
import ru.mentee.power.crm.repository.LeadRepository;

@SpringBootTest
@Transactional
class LeadServiceIntegrationTest {

  @Autowired
  private LeadService leadService;

  @Autowired
  private LeadRepository leadRepository;

  @Autowired
  private DealRepository dealRepository;

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

  private Lead createQualifiedLead() {
    Lead lead = new Lead();
    lead.setFirstName("Qualified");
    lead.setEmail("qualified_" + UUID.randomUUID() + "@test.com");
    lead.setPhone("+1234567890");
    lead.setCompany("Qualified Corp");
    lead.setStatus("QUALIFIED");
    lead.setCreatedAt(LocalDateTime.now());
    return leadRepository.save(lead);
  }

  @Test
  void convertLeadToDeal_shouldCommitOnSuccess() {
    Lead lead = createQualifiedLead();
    CreateDealRequest request = new CreateDealRequest();
    request.setAmount(new BigDecimal("10000.00"));

    Deal deal = leadService.convertLeadToDeal(lead.getId(), request);

    assertThat(deal).isNotNull();
    assertThat(deal.getLeadId()).isEqualTo(lead.getId().toString());
    assertThat(deal.getAmount()).isEqualTo(new BigDecimal("10000.00"));

    Lead updatedLead = leadRepository.findById(lead.getId()).orElseThrow();
    assertThat(updatedLead.getStatus()).isEqualTo("CONVERTED");

    assertThat(dealRepository.findById(deal.getId())).isPresent();
  }

  @Test
  void convertLeadToDeal_shouldRollbackOnConstraintViolation() {
    Lead lead = createQualifiedLead();
    CreateDealRequest invalidRequest = new CreateDealRequest();
    invalidRequest.setAmount(null);

    assertThatThrownBy(() -> leadService.convertLeadToDeal(lead.getId(), invalidRequest))
            .isInstanceOf(Exception.class);

    Lead unchangedLead = leadRepository.findById(lead.getId()).orElseThrow();
    assertThat(unchangedLead.getStatus()).isEqualTo("QUALIFIED");

    boolean dealExists = dealRepository.findAll().stream()
            .anyMatch(d -> d.getLeadId().equals(lead.getId().toString()));
    assertThat(dealExists).isFalse();
  }

  @Test
  void findLeads_shouldFilterByToDateTime() {
    Lead leadNow = new Lead();
    leadNow.setFirstName("Now");
    leadNow.setEmail("now2@test.com");
    leadNow.setPhone("+3333333333");
    leadNow.setCompany("NowCorp");
    leadNow.setStatus("NEW");
    leadNow.setCreatedAt(LocalDateTime.now());
    leadRepository.save(leadNow);

    Lead leadPast = new Lead();
    leadPast.setFirstName("Past");
    leadPast.setEmail("past2@test.com");
    leadPast.setPhone("+4444444444");
    leadPast.setCompany("PastCorp");
    leadPast.setStatus("NEW");
    leadPast.setCreatedAt(LocalDateTime.now().minusDays(2));
    leadRepository.save(leadPast);

    LocalDateTime toDate = LocalDateTime.now().minusDays(1);
    List<Lead> result = leadService.findLeads(null, null, null, toDate);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getEmail()).isEqualTo("past2@test.com");
  }

  @Test
  void findByEmailDerived_shouldReturnLead() {
    Lead lead = new Lead();
    lead.setFirstName("Derived");
    lead.setEmail("derived@test.com");
    lead.setPhone("+5555555555");
    lead.setCompany("DerivedCorp");
    lead.setStatus("NEW");
    lead.setCreatedAt(LocalDateTime.now());
    leadRepository.save(lead);

    Optional<Lead> found = leadService.findByEmailDerived("derived@test.com");
    assertThat(found).isPresent();
    assertThat(found.get().getEmail()).isEqualTo("derived@test.com");
  }

  @Test
  void getLeadsByCompany_shouldReturnPagedResults() {
    String company = "PageCompany";
    for (int i = 1; i <= 5; i++) {
      Lead lead = new Lead();
      lead.setFirstName("Page" + i);
      lead.setEmail("page" + i + "@test.com");
      lead.setPhone("+900000000" + i);
      lead.setCompany(company);
      lead.setStatus("NEW");
      lead.setCreatedAt(LocalDateTime.now().minusHours(i));
      leadRepository.save(lead);
    }

    Page<Lead> page = leadService.getLeadsByCompany(company, 0, 2);
    assertThat(page.getContent()).hasSize(2);
    assertThat(page.getTotalElements()).isEqualTo(5);
    assertThat(page.getTotalPages()).isEqualTo(3);
    assertThat(page.getNumber()).isEqualTo(0);

    Page<Lead> secondPage = leadService.getLeadsByCompany(company, 1, 2);
    assertThat(secondPage.getContent()).hasSize(2);
    assertThat(secondPage.getNumber()).isEqualTo(1);
  }

  @Test
  void convertLeadToDeal_shouldThrowExceptionWhenLeadNotQualified() {
    Lead lead = createLeadWithStatus("NEW");
    leadRepository.save(lead);

    CreateDealRequest request = new CreateDealRequest();
    request.setAmount(new BigDecimal("5000"));

    assertThatThrownBy(() -> leadService.convertLeadToDeal(lead.getId(), request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("cannot be converted");
  }

  private Lead createLeadWithStatus(String status) {
    Lead lead = new Lead();
    lead.setFirstName("StatusTest");
    lead.setEmail("status_" + UUID.randomUUID() + "@test.com");
    lead.setPhone("+0000000000");
    lead.setCompany("StatusCorp");
    lead.setStatus(status);
    lead.setCreatedAt(LocalDateTime.now());
    return lead;
  }
}