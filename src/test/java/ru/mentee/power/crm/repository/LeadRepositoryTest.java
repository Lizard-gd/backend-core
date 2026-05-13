package ru.mentee.power.crm.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import ru.mentee.power.crm.model.Lead;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LeadRepositoryTest {

  @Autowired
  private LeadRepository repository;

  private Lead createTestLead(String email, String status) {
    Lead lead = new Lead();
    lead.setFirstName("TestName");
    lead.setEmail(email);
    lead.setPhone("+71234567890");
    lead.setCompany("TestCompany");
    lead.setStatus(status);
    lead.setCreatedAt(LocalDateTime.now());
    // ID не задаём – пусть Hibernate сгенерирует
    return lead;
  }

  @Test
  void shouldSaveAndFindLeadById_whenValidData() {
    Lead lead = createTestLead("save@example.com", "NEW");

    Lead saved = repository.save(lead);
    Optional<Lead> found = repository.findById(saved.getId());

    assertThat(found).isPresent();
    assertThat(found.get().getEmail()).isEqualTo("save@example.com");
    assertThat(found.get().getStatus()).isEqualTo("NEW");
  }

  @Test
  void shouldFindByEmailNative_whenLeadExists() {
    Lead lead = createTestLead("native@test.com", "QUALIFIED");
    repository.save(lead);

    Optional<Lead> found = repository.findByEmailNative("native@test.com");

    assertThat(found).isPresent();
    assertThat(found.get().getCompany()).isEqualTo("TestCompany");
  }

  @Test
  void shouldReturnEmptyOptional_whenEmailNotFound() {
    Optional<Lead> found = repository.findByEmailNative("notexist@example.com");
    assertThat(found).isEmpty();
  }

  @Test
  void shouldFindByStatusNative_whenLeadExists() {
    Lead lead = createTestLead("status@test.com", "CONTACTED");
    repository.save(lead);

    List<Lead> found = repository.findByStatusNative("CONTACTED");

    assertThat(found).hasSize(1);
    assertThat(found.get(0).getEmail()).isEqualTo("status@test.com");
  }

  @Test
  void shouldFindAllLeads_whenMultipleSaved() {
    repository.save(createTestLead("all1@test.com", "NEW"));
    repository.save(createTestLead("all2@test.com", "QUALIFIED"));

    List<Lead> all = repository.findAll();

    assertThat(all).hasSize(2);
  }

  @Test
  void shouldDeleteLeadById_whenLeadExists() {
    Lead lead = createTestLead("delete@test.com", "NEW");
    Lead saved = repository.save(lead);
    UUID id = saved.getId();

    repository.deleteById(id);
    Optional<Lead> found = repository.findById(id);

    assertThat(found).isEmpty();
  }
}