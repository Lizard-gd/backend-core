package ru.mentee.power.crm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.repository.LeadRepository;

class LeadServiceTest {
  private LeadService service;
  private LeadRepository repository;

  @BeforeEach
  void setUp() {
    repository = new LeadRepository();
    service = new LeadService(repository);
  }

  @Test
  void shouldCreateLead_whenEmailIsUnique() {
    String email = "unique@example.com";
    String phone = "+123456789";
    String company = "Test Corp";
    String status = "NEW";

    Lead result = service.addLead(email, phone, company, status);

    assertThat(result).isNotNull();
    assertThat(result.email()).isEqualTo(email);
    assertThat(result.phone()).isEqualTo(phone);
    assertThat(result.company()).isEqualTo(company);
    assertThat(result.status()).isEqualTo(status);
    assertThat(result.id()).isNotNull();
  }

  @Test
  void shouldThrowException_whenEmailAlreadyExists() {
    String email = "duplicate@example.com";
    service.addLead(email, "111111", "First Corp", "NEW");

    assertThatThrownBy(() ->
            service.addLead(email, "222222", "Second Corp", "NEW")
    )
      .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Lead with email already exists");
  }

  @Test
  void shouldFindAllLeads() {
    service.addLead("one@example.com", "111", "Company A", "NEW");
    service.addLead("two@example.com", "222", "Company B", "QUALIFIED");

    List<Lead> result = service.findAll();

    assertThat(result).hasSize(2);
  }

  @Test
  void shouldFindLeadById() {
    Lead created = service.addLead("find@example.com", "333", "Some Corp", "NEW");

    Optional<Lead> result = service.findById(created.id());

    assertThat(result).isPresent();
    assertThat(result.get().email()).isEqualTo("find@example.com");
  }

  @Test
  void shouldReturnEmpty_whenLeadNotFoundById() {
    Optional<Lead> result = service.findById("non-existent-id-12345");

    assertThat(result).isEmpty();
  }

  @Test
  void shouldFindLeadByEmail() {
    service.addLead("search@example.com", "444", "Search Corp", "NEW");

    Optional<Lead> result = service.findByEmail("search@example.com");

    assertThat(result).isPresent();
    assertThat(result.get().company()).isEqualTo("Search Corp");
  }

  @Test
  void shouldReturnEmpty_whenLeadNotFoundByEmail() {
    Optional<Lead> result = service.findByEmail("notfound@example.com");

    assertThat(result).isEmpty();
  }
}
