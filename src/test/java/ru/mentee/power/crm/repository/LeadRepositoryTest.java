package ru.mentee.power.crm.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mentee.power.crm.model.Lead;

public class LeadRepositoryTest {
  private LeadRepository repository;

  @BeforeEach
  void setUp() {
    repository = new LeadRepository();
  }

  @Test
  void shouldSaveAndFindLeadById_whenLeadSaved() {

    Lead lead = new Lead("1", "Ivan",
            "ivan@mail.ru", "+71234", "TestCorp", "NEW", LocalDateTime.now());

    repository.save(lead);
    Optional<Lead> found = repository.findById("1");

    assertThat(found).isPresent();
    assertThat(found.get().email()).isEqualTo("ivan@mail.ru");
  }

  @Test
  void shouldReturnNull_whenLeadNotFound() {

    Optional<Lead> found = repository.findById("unknown-id");

    assertThat(found).isEmpty();
  }

  @Test
  void shouldReturnAllLeads_whenMultipleLeadsSaved() {
    Lead lead1 = new Lead("1", "Ivan",
            "ivan@mail.ru", "+71234", "TestCorp", "NEW", LocalDateTime.now());
    Lead lead2 = new Lead("2", "pavel",
            "pavel@mail.ru", "+75678", "TestCorp", "NEW", LocalDateTime.now());
    Lead lead3 = new Lead("3", "marina",
            "marina@mail.ru", "+74321", "TestCorp", "NEW", LocalDateTime.now());

    repository.save(lead1);
    repository.save(lead2);
    repository.save(lead3);

    List<Lead> leads = repository.findAll();

    assertThat(leads).hasSize(3);
  }

  @Test
  void shouldDeleteLead_whenLeadExists() {
    Lead lead1 = new Lead("1", "Ivan", "ivan@mail.ru",
            "+71234", "TestCorp", "NEW", LocalDateTime.now());
    repository.save(lead1);

    repository.delete("1");

    assertThat(repository.findById("1")).isEmpty();
    assertThat(repository.size()).isEqualTo(0);
  }

  @Test
  void shouldOverwriteLead_whenSaveWithSameId() {
    Lead lead1 = new Lead("1", "Ivan", "ivan@mail.ru",
            "+71234", "TestCorp", "NEW", LocalDateTime.now());
    Lead lead2 = new Lead("1", "pavel", "pavel@mail.ru",
            "+75678", "TestCorp", "NEW", LocalDateTime.now());

    repository.save(lead1);
    repository.save(lead2);

    assertThat(repository.size()).isEqualTo(1);
    assertThat(repository.findById("1").get().email()).isEqualTo("pavel@mail.ru");
  }

  @Test
  void shouldFindFasterWithMap_thanWithListFilter() {
    List<Lead> leadList = new ArrayList<>();

    for (int i = 0; i < 1000; i++) {
      String id = String.valueOf(i);
      Lead lead = new Lead(id, "Ivan" + i, "email" + i + "@test.com",
              "+7" + i, "Company" + i, "NEW", LocalDateTime.now());
      repository.save(lead);
      leadList.add(lead);
    }
    String targetId = "500";

    long mapStart = System.nanoTime();
    Optional<Lead> foundInMap = repository.findById(targetId);
    long mapDuration = System.nanoTime() - mapStart;

    long listStart = System.nanoTime();
    Lead foundInList = leadList.stream()
            .filter(lead -> lead.id().equals(targetId))
            .findFirst()
            .orElse(null);
    long listDuration = System.nanoTime() - listStart;

    assertThat(foundInMap.get()).isEqualTo(foundInList);

    assertThat(listDuration).isGreaterThan(mapDuration * 10);

    System.out.println("Map поиск: " + mapDuration + " ns");
    System.out.println("List поиск: " + listDuration + " ns");
    System.out.println("Ускорение: " + (listDuration / mapDuration) + "x");
  }

  @Test
  void shouldSaveBothLeads_evenWithSameEmailAndPhone_becauseRepositoryDoesNotCheckBusinessRules() {
    Lead originalLead = new Lead("1", "Ivan", "ivan@mail.ru",
            "+79001234567", "Acme Corp", "NEW", LocalDateTime.now());
    Lead duplicateLead = new Lead("2", "Ivan", "ivan@mail.ru",
            "+79001234567", "TechCorp", "HOT", LocalDateTime.now());

    repository.save(originalLead);
    repository.save(duplicateLead);

    assertThat(repository.size()).isEqualTo(2);
  }
}
