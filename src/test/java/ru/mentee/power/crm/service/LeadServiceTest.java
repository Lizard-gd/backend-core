package ru.mentee.power.crm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
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
    String firstName = "Ivan";
    String email = "unique@example.com";
    String phone = "+123456789";
    String company = "Test Corp";
    String status = "NEW";

    Lead result = service.addLead(firstName, email, phone, company, status);

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
    service.addLead("ivan", email, "111111", "First Corp", "NEW");

    assertThatThrownBy(() ->
            service.addLead("ivan", email, "222222", "Second Corp", "NEW")
    )
      .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Lead with email already exists");
  }

  @Test
  void shouldFindAllLeads() {
    service.addLead("ivan", "one@example.com", "111", "Company A", "NEW");
    service.addLead("ivan", "two@example.com", "222", "Company B", "QUALIFIED");

    List<Lead> result = service.findAll();

    assertThat(result).hasSize(2);
  }

  @Test
  void shouldFindLeadById() {
    Lead created = service.addLead("ivan", "find@example.com", "333", "Some Corp", "NEW");

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
    service.addLead("Ivan", "search@example.com", "444", "Search Corp", "NEW");

    Optional<Lead> result = service.findByEmail("search@example.com");

    assertThat(result).isPresent();
    assertThat(result.get().company()).isEqualTo("Search Corp");
  }

  @Test
  void shouldReturnEmpty_whenLeadNotFoundByEmail() {
    Optional<Lead> result = service.findByEmail("notfound@example.com");

    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnOnlyNewLeads_whenFindByStatusNew() {
    // Given
    service.addLead("Ivan", "new1@example.com", "+111", "Company1", "NEW");
    service.addLead("Ivan", "new2@example.com", "+222", "Company2", "NEW");
    service.addLead("Ivan", "contacted@example.com", "+333", "Company3", "CONTACTED");
    service.addLead("Ivan", "qualified@example.com", "+444", "Company4", "QUALIFIED");

    // When
    List<Lead> result = service.findByStatus("NEW");

    // Then
    assertThat(result).hasSize(2);
    assertThat(result).allMatch(lead -> lead.status().equals("NEW"));
  }

  @Test
  void shouldReturnEmptyList_whenNoLeadsWithGivenStatus() {
    // Given
    service.addLead("Ivan", "new@example.com", "+111", "Company1", "NEW");
    service.addLead("Ivan", "contacted@example.com", "+222", "Company2", "CONTACTED");

    // When
    List<Lead> result = service.findByStatus("QUALIFIED");

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnOnlyContactedLeads_whenFindByStatusContacted() {
    // Given
    service.addLead("Ivan", "new@example.com", "+111", "Company1", "NEW");
    service.addLead("Ivan", "contacted1@example.com", "+222", "Company2", "CONTACTED");
    service.addLead("Ivan", "contacted2@example.com", "+333", "Company3", "CONTACTED");
    service.addLead("Ivan", "qualified@example.com", "+444", "Company4", "QUALIFIED");

    // When
    List<Lead> result = service.findByStatus("CONTACTED");

    // Then
    assertThat(result).hasSize(2);
    assertThat(result).allMatch(lead -> lead.status().equals("CONTACTED"));
  }

  @Test
  void shouldReturnOnlyQualifiedLeads_whenFindByStatusQualified() {
    // Given
    service.addLead("Ivan", "new@example.com", "+111", "Company1", "NEW");
    service.addLead("Ivan", "contacted@example.com", "+222", "Company2", "CONTACTED");
    service.addLead("Ivan", "qualified1@example.com", "+333", "Company3", "QUALIFIED");
    service.addLead("Ivan", "qualified2@example.com", "+444", "Company4", "QUALIFIED");

    // When
    List<Lead> result = service.findByStatus("QUALIFIED");

    // Then
    assertThat(result).hasSize(2);
    assertThat(result).allMatch(lead -> lead.status().equals("QUALIFIED"));
  }

  @Test
  @DisplayName("update должен обновить существующего лида, не меняя createdAt")
  void shouldUpdateExistingLead() {
    // given
    Lead created = service.addLead("John", "john@update.com", "+111", "OldCorp", "NEW");
    String id = created.id();

    // when
    Lead updatedLead = new Lead(id, "Johnny", "john@update.com",
            "+111", "NewCorp", "QUALIFIED", created.createdAt());
    Lead result = service.update(id, updatedLead);

    // then
    assertThat(result.firstName()).isEqualTo("Johnny");
    assertThat(result.company()).isEqualTo("NewCorp");
    assertThat(result.status()).isEqualTo("QUALIFIED");
    assertThat(result.createdAt()).isEqualTo(created.createdAt()); // дата не изменилась

    Optional<Lead> found = service.findById(id);
    assertThat(found).isPresent();
    assertThat(found.get().firstName()).isEqualTo("Johnny");
  }

  @Test
  @DisplayName("update должен выбросить IllegalArgumentException "
          + "при обновлении несуществующего лида")
  void shouldThrowWhenUpdateNonExistingLead() {
    // given
    String nonExistentId = "non-existent-id";
    Lead dummyLead = new Lead(nonExistentId, "Nobody", "nobody@example.com",
            "+999", "NoCorp", "LOST", LocalDateTime.now());

    // when/then – покрывает ветку `if (existing.isEmpty())`
    assertThatThrownBy(() -> service.update(nonExistentId, dummyLead))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Lead not found with id: " + nonExistentId);
  }

  @Test
  @DisplayName("delete должен удалить существующего лида")
  void shouldDeleteExistingLead() {
    // given
    Lead created = service.addLead("Del", "delete@example.com", "+000", "ToDelete", "NEW");
    String id = created.id();
    assertThat(service.findById(id)).isPresent();

    // when
    service.delete(id);

    // then
    assertThat(service.findById(id)).isEmpty();
    assertThat(service.findAll()).doesNotContain(created);
  }

  @Test
  @DisplayName("delete должен выбросить ResponseStatusException "
          + "(404) при удалении несуществующего лида")
  void shouldThrowWhenDeleteNonExistingLead() {
    // given
    String nonExistentId = "non-existent-id";

    // when/then – покрывает ветку `if (existing.isEmpty())` в delete
    assertThatThrownBy(() -> service.delete(nonExistentId))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  @DisplayName("findLeads без параметров возвращает всех лидов")
  void findLeads_NoParams_ReturnsAll() {
    service.addLead("Alice", "alice@test.com", "+1", "A", "NEW");
    service.addLead("Bob", "bob@test.com", "+2", "B", "QUALIFIED");
    int allCount = service.findAll().size();

    List<Lead> result = service.findLeads(null, null, null, null);

    assertThat(result).hasSize(allCount);
  }

  @Test
  @DisplayName("findLeads фильтрует по email (search)")
  void findLeads_FiltersByEmail() {
    service.addLead("Alice", "alice@test.com", "+1", "A", "NEW");
    service.addLead("Bob", "bob@example.com", "+2", "B", "NEW");

    List<Lead> result = service.findLeads("test", null, null, null);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).email()).isEqualTo("alice@test.com");
  }

  @Test
  @DisplayName("findLeads фильтрует по имени (search)")
  void findLeads_FiltersByFirstName() {
    service.addLead("Alice", "aa@test.com", "+1", "A", "NEW");
    service.addLead("Bob", "bb@test.com", "+2", "B", "NEW");

    List<Lead> result = service.findLeads("Ali", null, null, null);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).firstName()).isEqualTo("Alice");
  }

  @Test
  @DisplayName("findLeads фильтрует по статусу")
  void findLeads_FiltersByStatus() {
    service.addLead("John", "john@x.com", "+1", "X", "NEW");
    service.addLead("Jane", "jane@x.com", "+2", "X", "QUALIFIED");

    List<Lead> result = service.findLeads(null, "NEW", null, null);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).status()).isEqualTo("NEW");
  }

  @Test
  @DisplayName("findLeads фильтрует по fromDateTime (дата создания не ранее)")
  void findLeads_FiltersByFromDateTime() {
    // создаём двух лидов с разницей во времени
    Lead early = service.addLead("Early", "early@test.com", "+1", "E", "NEW");
    try {
      Thread.sleep(100); } catch (InterruptedException ignored) {

    }
    Lead late = service.addLead("Late", "late@test.com", "+2", "L", "NEW");

    // from = момент создания позднего лида (включаем только позднего)
    LocalDateTime from = late.createdAt();
    List<Lead> result = service.findLeads(null, null, from, null);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).firstName()).isEqualTo("Late");
  }

  @Test
  @DisplayName("findLeads фильтрует по toDateTime (дата создания не позже)")
  void findLeads_FiltersByToDateTime() {
    Lead early = service.addLead("Early", "early@test.com", "+1", "E", "NEW");
    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
    Lead late = service.addLead("Late", "late@test.com", "+2", "L", "NEW");

    // to = момент создания раннего лида (включаем только раннего)
    LocalDateTime to = early.createdAt();
    List<Lead> result = service.findLeads(null, null, null, to);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).firstName()).isEqualTo("Early");
  }

  @Test
  @DisplayName("findLeads комбинирует поиск, статус и даты")
  void findLeads_CombinesFilters() {
    service.addLead("Alice", "alice@x.com", "+1", "A", "NEW");
    service.addLead("Bob", "bob@x.com", "+2", "B", "QUALIFIED");

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime from = now.minusMinutes(1);
    LocalDateTime to = now.plusMinutes(1);

    List<Lead> result = service.findLeads("Alice", "NEW", from, to);
    assertThat(result).hasSize(1);
    assertThat(result.get(0).firstName()).isEqualTo("Alice");
  }
}
