package ru.mentee.power.crm.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mentee.power.crm.domain.Address;
import ru.mentee.power.crm.domain.Contact;
import ru.mentee.power.crm.domain.Lead;

class InMemoryLeadRepositoryTest {

  private InMemoryLeadRepository repository;
  private Contact testContact;

  @BeforeEach
  void setUp() {
    repository = new InMemoryLeadRepository();
    Address address = new Address("Moscow", "Tverskaya", "101000");
    testContact = new Contact("John", "Doe", "test@example.com", "+79991234567", address);
  }

  @Test
  void addUnique_shouldAddLead() {
    Lead lead = new Lead(UUID.randomUUID(), testContact, "Company", "NEW");

    repository.add(lead);

    assertThat(repository.findAll()).hasSize(1);
    assertThat(repository.findById(lead.id())).contains(lead);
  }

  @Test
  void addDuplicate_shouldNotAdd() {
    UUID id = UUID.randomUUID();
    Lead lead1 = new Lead(id, testContact, "Company", "NEW");
    Lead lead2 = new Lead(id, testContact, "Company", "NEW");

    repository.add(lead1);
    repository.add(lead2);

    assertThat(repository.findAll()).hasSize(1);
  }

  @Test
  void findById_shouldReturnLead_whenExists() {
    Lead lead = new Lead(UUID.randomUUID(), testContact, "Company", "NEW");
    repository.add(lead);

    Optional<Lead> found = repository.findById(lead.id());

    assertThat(found).isPresent();
    assertThat(found.get()).isEqualTo(lead);
  }

  @Test
  void findById_shouldReturnEmpty_whenNotExists() {
    UUID nonExistendId = UUID.randomUUID();

    Optional<Lead> found = repository.findById(nonExistendId);

    assertThat(found).isEmpty();
  }

  @Test
  void remove_shouldDeleteLead() {
    Lead lead = new Lead(UUID.randomUUID(), testContact, "Company", "NEW");
    repository.add(lead);
    assertThat(repository.findAll()).hasSize(1);

    repository.remove(lead.id());

    assertThat(repository.findAll()).isEmpty();
    assertThat(repository.findById(lead.id())).isEmpty();
  }

  @Test
  void findAll_shouldReturnDefensiveCopy() {
    Lead lead = new Lead(UUID.randomUUID(), testContact, "Company", "NEW");
    repository.add(lead);

    List<Lead> returnList = repository.findAll();
    returnList.clear(); // пытаемся испортить

    assertThat(repository.findAll()).hasSize(1); // оригинал не пострадал
  }
}
