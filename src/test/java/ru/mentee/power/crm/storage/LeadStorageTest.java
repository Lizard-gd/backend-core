package ru.mentee.power.crm.storage;

import org.junit.jupiter.api.Test;
import ru.mentee.power.crm.domain.Lead;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LeadStorageTest {

  @Test
    void shouldAddLead_whenLeadIsUnique() {
        // Given: создаем пустое хранилище и уникального лида
    LeadStorage storage = new LeadStorage();
    Lead uniqueLead = new Lead("1", "ivan@mail.ru", "+7123", "TechCorp", "NEW");

        // When: добавляем лида
    boolean added = storage.add(uniqueLead);

        // Then: проверяем результат
    assertThat(added).isTrue();
    assertThat(storage.size()).isEqualTo(1);
    assertThat(storage.findAll()).containsExactly(uniqueLead);
  }

  @Test
    void shouldRejectDuplicate_whenEmailAlreadyExists() {
        // Given: хранилище с существующим лидом
    LeadStorage storage = new LeadStorage();
    Lead existingLead = new Lead("1", "ivan@mail.ru", "+7123", "TechCorp", "NEW");
    Lead duplicateLead = new Lead("2", "ivan@mail.ru", "+7456", "Other", "NEW");
    storage.add(existingLead); // добавляем первого лида

        // When: пытаемся добавить дубликат (такой же email)
    boolean added = storage.add(duplicateLead);

        // Then: проверяем, что дубликат не добавился
    assertThat(added).isFalse(); // метод вернул false
    assertThat(storage.size()).isEqualTo(1); // размер не изменился
    assertThat(storage.findAll()).containsExactly(existingLead); // все еще только первый лид
  }

  @Test
    void shouldThrowException_whenStorageIsFull() {
        // Given: заполняем хранилище 100 лидами
    LeadStorage storage = new LeadStorage();
    for (int i = 0; i < 100; i++) {
      storage.add(new Lead(String.valueOf(i), "lead" + i + "@mail.ru", "+7000", "Company", "NEW"));
    }
        // When + Then: пытаемся добавить 101-й лид, ожидаем исключение
    Lead hundredFirstLead = new Lead("101", "lead101@mail.ru", "+7001", "Company", "NEW");

    assertThatThrownBy(() -> storage.add(hundredFirstLead))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Storage is full");
  }

  @Test
    void shouldReturnOnlyAddedLeads_whenFindAllCalled() {
        // Given: хранилище с двумя добавленными лидами
    LeadStorage storage = new LeadStorage();
    Lead firstLead = new Lead("1", "ivan@mail.ru", "+7123", "TechCorp", "NEW");
    Lead secondLead = new Lead("2", "maria@startup.io", "+7456", "StartupLab", "NEW");
    storage.add(firstLead);
    storage.add(secondLead);

        // When: вызываем findAll()
    Lead[] result = storage.findAll();

        // Then: проверяем, что возвращены только добавленные лиды
    assertThat(result).hasSize(2);
    assertThat(result).containsExactly(firstLead, secondLead);
  }
}
