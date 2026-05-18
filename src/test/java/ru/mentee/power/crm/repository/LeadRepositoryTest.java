package ru.mentee.power.crm.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.mentee.power.crm.model.Company;
import ru.mentee.power.crm.model.Lead;

@DataJpaTest
class LeadRepositoryTest {

  @Autowired
  private LeadRepository leadRepository;

  @Autowired
  private CompanyRepository companyRepository;

  private Lead lead1;
  private Lead lead2;
  private Company companyTinkoff;
  private Company companySber;

  @BeforeEach
  void setUp() {
    leadRepository.deleteAll();
    companyRepository.deleteAll();

    companyTinkoff = new Company();
    companyTinkoff.setName("Тинькофф");
    companyTinkoff.setIndustry("Finance");
    companyRepository.save(companyTinkoff);

    companySber = new Company();
    companySber.setName("Сбербанк");
    companySber.setIndustry("Banking");
    companyRepository.save(companySber);

    lead1 = new Lead();
    lead1.setFirstName("John");
    lead1.setEmail("john@example.com");
    lead1.setPhone("+123456789");
    lead1.setCompany(companyTinkoff);
    lead1.setStatus("NEW");
    lead1.setCreatedAt(LocalDateTime.now().minusDays(5));
    leadRepository.save(lead1);

    lead2 = new Lead();
    lead2.setFirstName("Jane");
    lead2.setEmail("jane@example.com");
    lead2.setPhone("+987654321");
    lead2.setCompany(companyTinkoff);
    lead2.setStatus("CONTACTED");
    lead2.setCreatedAt(LocalDateTime.now().minusDays(2));
    leadRepository.save(lead2);
  }

  @Test
  void shouldSaveAndFindLeadById_whenValidData() {
    Lead lead = new Lead();
    lead.setFirstName("Test");
    lead.setEmail("save@example.com");
    lead.setPhone("+71234567890");
    lead.setCompany(companySber);
    lead.setStatus("NEW");
    lead.setCreatedAt(LocalDateTime.now());

    Lead saved = leadRepository.save(lead);
    Optional<Lead> found = leadRepository.findById(saved.getId());

    assertThat(found).isPresent();
    assertThat(found.get().getEmail()).isEqualTo("save@example.com");
    assertThat(found.get().getStatus()).isEqualTo("NEW");
    assertThat(found.get().getCompany().getName()).isEqualTo("Сбербанк");
  }

  @Test
  void shouldFindByEmailNative_whenLeadExists() {
    Optional<Lead> found = leadRepository.findByEmailNative("john@example.com");

    assertThat(found).isPresent();
    assertThat(found.get().getCompany().getName()).isEqualTo("Тинькофф");
  }

  @Test
  void shouldReturnEmptyOptional_whenEmailNotFound() {
    Optional<Lead> found = leadRepository.findByEmailNative("notexist@example.com");
    assertThat(found).isEmpty();
  }

  @Test
  void shouldFindByStatusNative_whenLeadExists() {
    Lead extraContacted = new Lead();
    extraContacted.setFirstName("Extra");
    extraContacted.setEmail("extra@test.com");
    extraContacted.setPhone("+111111111");
    extraContacted.setCompany(companySber);
    extraContacted.setStatus("CONTACTED");
    extraContacted.setCreatedAt(LocalDateTime.now());
    leadRepository.save(extraContacted);

    List<Lead> found = leadRepository.findByStatusNative("CONTACTED");

    assertThat(found).hasSize(2);
    assertThat(found).allMatch(lead -> lead.getStatus().equals("CONTACTED"));
  }

  @Test
  void shouldFindAllLeads_whenMultipleSaved() {
    List<Lead> all = leadRepository.findAll();
    assertThat(all).hasSize(2);
  }

  @Test
  void shouldDeleteLeadById_whenLeadExists() {
    UUID id = lead1.getId();
    leadRepository.deleteById(id);
    Optional<Lead> found = leadRepository.findById(id);
    assertThat(found).isEmpty();
  }

  @Test
  void findByCompanyId_shouldReturnLeads_whenCompanyMatches() {
    List<Lead> leads = leadRepository.findByCompanyId(companyTinkoff.getId());

    assertThat(leads).hasSize(2);
    assertThat(leads).extracting(Lead::getEmail)
            .containsExactlyInAnyOrder("john@example.com", "jane@example.com");
  }

  @Test
  void countByStatus_shouldReturnCorrectCount() {
    long newCount = leadRepository.countByStatus("NEW");
    long contactedCount = leadRepository.countByStatus("CONTACTED");

    assertThat(newCount).isEqualTo(1);
    assertThat(contactedCount).isEqualTo(1);
  }

  @Test
  void existsByEmail_shouldReturnTrue_whenEmailExists() {
    boolean exists = leadRepository.existsByEmail("john@example.com");
    boolean notExists = leadRepository.existsByEmail("missing@example.com");

    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  void findByStatusAndCompany_shouldReturnFilteredLeads() {
    List<Lead> result = leadRepository.findByStatusAndCompanyId("NEW", companyTinkoff.getId());
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getEmail()).isEqualTo("john@example.com");
  }

  @Test
  void findByStatusOrderByCreatedAtDesc_shouldReturnSortedLeads() {
    Lead lead3 = new Lead();
    lead3.setFirstName("Newer");
    lead3.setEmail("newer@example.com");
    lead3.setPhone("+999999999");
    lead3.setCompany(companySber);
    lead3.setStatus("NEW");
    lead3.setCreatedAt(LocalDateTime.now().minusDays(1));
    leadRepository.save(lead3);

    List<Lead> newLeads = leadRepository.findByStatusOrderByCreatedAtDesc("NEW");

    assertThat(newLeads).hasSize(2);
    assertThat(newLeads.get(0).getCreatedAt()).isAfter(newLeads.get(1).getCreatedAt());
    assertThat(newLeads.get(0).getEmail()).isEqualTo("newer@example.com");
  }

  @Test
  void findByStatusIn_shouldReturnLeadsWithAnyOfGivenStatuses() {
    List<Lead> found = leadRepository.findByStatusIn(List.of("NEW", "CONTACTED"));
    assertThat(found).hasSize(2);
  }

  @Test
  void findCreatedAfter_shouldReturnLeadsCreatedAfterDate() {
    LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
    List<Lead> result = leadRepository.findCreatedAfter(threeDaysAgo);
    // lead2 создан 2 дня назад, lead3 не создавался – должен быть только lead2
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getEmail()).isEqualTo("jane@example.com");
  }

  @Test
  void findAll_withPageable_shouldReturnPageWithMetadata() {
    PageRequest pageRequest = PageRequest.of(0, 1);
    Page<Lead> page = leadRepository.findAll(pageRequest);

    assertThat(page.getContent()).hasSize(1);
    assertThat(page.getTotalElements()).isEqualTo(2);
    assertThat(page.getTotalPages()).isEqualTo(2);
    assertThat(page.getNumber()).isEqualTo(0);
    assertThat(page.hasNext()).isTrue();
  }

  @Test
  void findByStatus_withPageable_shouldReturnPagedResults() {
    Page<Lead> page = leadRepository.findByStatus("NEW", PageRequest.of(0, 10));
    assertThat(page.getContent()).hasSize(1);
    assertThat(page.getTotalElements()).isEqualTo(1);
  }

  @Test
  void findByCompanyId_withPageable_shouldReturnPage() {
    // Используем новый метод с пагинацией
    Page<Lead> page = leadRepository.findByCompanyId(companyTinkoff.getId(), PageRequest.of(0, 10));
    assertThat(page.getContent()).hasSize(2);
    assertThat(page.getTotalElements()).isEqualTo(2);
  }

  @Test
  void findByStatusInPaged_shouldReturnPage() {
    Page<Lead> page = leadRepository.findByStatusInPaged(List.of("NEW", "CONTACTED"),
            PageRequest.of(0, 10));
    assertThat(page.getContent()).hasSize(2);
    assertThat(page.getTotalElements()).isEqualTo(2);
  }
}