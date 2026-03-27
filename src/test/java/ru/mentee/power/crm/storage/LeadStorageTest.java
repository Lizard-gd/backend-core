package ru.mentee.power.crm.storage;

import org.junit.jupiter.api.Test;
import ru.mentee.power.crm.domain.Address;
import ru.mentee.power.crm.domain.Contact;
import ru.mentee.power.crm.domain.Lead;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LeadStorageTest {

  private static final UUID TEST_ID_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID TEST_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID TEST_ID_3 = UUID.fromString("00000000-0000-0000-0000-000000000003");

  @Test
    void shouldAddLead_whenLeadIsUnique() {
        // Given: создаем пустое хранилище и уникального лида
    LeadStorage storage = new LeadStorage();

    Address address = new Address("San Francisco", "123 Main St", "94105");
    Contact contact = new Contact("Ivan", "Petrov", "ivan@mail.ru", "+7123", address);
    Lead uniqueLead = new Lead(TEST_ID_1, contact, "TechCorp", "NEW");

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

    Address address = new Address("San Francisco", "123 Main St", "94105");
    Contact contact = new Contact("Ivan", "Petrov", "ivan@mail.ru", "+7123", address);
    Lead uniqueLead = new Lead(TEST_ID_1, contact, "TechCorp", "NEW");

    Lead existingLead = new Lead(TEST_ID_1, contact, "TechCorp", "NEW");
    Lead duplicateLead = new Lead(TEST_ID_2, contact, "Other", "NEW");
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
      Address address = new Address("City" + i, "Street" + i, "Zip" + i);
      Contact contact = new Contact("Name" + i, "LastName" + i,
              "lead" + i + "@mail.ru", "+7000", address);
      Lead lead = new Lead(UUID.randomUUID(), contact, "Company", "NEW");
      storage.add(lead);
    }
        // When + Then: пытаемся добавить 101-й лид, ожидаем исключение
    Address lastAddress = new Address("LastCity", "LastStreet", "LastZip");
    Contact lastContact = new Contact("Last", "Name", "lead101@mail.ru", "+7001", lastAddress);
    Lead hundredFirstLead = new Lead(UUID.randomUUID(), lastContact, "Company", "NEW");

    assertThatThrownBy(() -> storage.add(hundredFirstLead))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Storage is full");
  }

  @Test
    void shouldReturnOnlyAddedLeads_whenFindAllCalled() {
        // Given: хранилище с двумя добавленными лидами
    LeadStorage storage = new LeadStorage();

    Address address1 = new Address("San Francisco", "123 Main St", "94105");
    Contact contact1 = new Contact("Ivan", "Petrov", "ivan@mail.ru", "+7123", address1);
    Lead firstLead = new Lead(TEST_ID_1, contact1, "TechCorp", "NEW");

    Address address2 = new Address("New York", "456 Broadway", "10001");
    Contact contact2 = new Contact("Maria", "Ivanova", "maria@startup.io", "+7456", address2);
    Lead secondLead = new Lead(TEST_ID_2, contact2, "StartupLab", "NEW");

    storage.add(firstLead);
    storage.add(secondLead);

        // When: вызываем findAll()
    Lead[] result = storage.findAll();

        // Then: проверяем, что возвращены только добавленные лиды
    assertThat(result).hasSize(2);
    assertThat(result).containsExactly(firstLead, secondLead);
  }
}
