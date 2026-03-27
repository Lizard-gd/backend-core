package ru.mentee.power.crm.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeadTest {

  @Test
  void shouldCreateLead_whenValidDate() {
      // 1. Создаем Address, Contact с Address
    Address address = new Address("San Francisco", "123 Main St", "94105");
    Contact contact = new Contact("John", "Doe", "john@example.com", "+71234", address);

      // 2. Создаем UUID и Lead
    UUID expectedId = UUID.randomUUID(); // сохраняем ID
    Lead lead = new Lead(expectedId, contact, "TestCorp", "NEW");

      // 3. Проверяем
    assertThat(lead.id()).isEqualTo(expectedId);
    assertThat(lead.contact()).isEqualTo(contact);
    assertThat(lead.company()).isEqualTo("TestCorp");
    assertThat(lead.status()).isEqualTo("NEW");

      // 4. Проверяем делегацию (доступ через Contact)
    assertThat(lead.contact().email()).isEqualTo("john@example.com");
    assertThat(lead.contact().phone()).isEqualTo("+71234");

      // 5. Проверяем трехуровневую делегацию (Lead → Contact → Address)
    assertThat(lead.contact().address().city()).isEqualTo("San Francisco");
    assertThat(lead.contact().address().street()).isEqualTo("123 Main St");
    assertThat(lead.contact().address().zip()).isEqualTo("94105");
  }

  @Test
  void shouldAccessEmailThroughDelegation_whenLeadCreated() {
    Address address = new Address("San Francisco", "123 Main St", "94105");
    Contact contact = new Contact("John", "Doe", "john@example.com", "+71234", address);

    UUID expectedId = UUID.randomUUID();
    Lead lead = new Lead(expectedId, contact, "TestCorp", "NEW");

    String email = lead.contact().email();
    assertThat(email).isEqualTo("john@example.com");

    String city = lead.contact().address().city();
    assertThat(city).isEqualTo("San Francisco");
  }

  @Test
  void shouldBeEqual_whenSameIdButDifferentContact() {
      // 1. Два разных Address
    Address address1 = new Address("San Francisco", "123 Main St", "94105");
    Address address2 = new Address("New York", "456 Broadway", "10001");

      // 2. Два разных Contact (с разными address)
    Contact contact1 = new Contact("John", "Doe", "john@example.com", "+71234", address1);
    Contact contact2 = new Contact("Jane", "Smith", "jane@example.com", "+56789", address2);

      // 3. Два Lead с ОДИНАКОВЫМ UUID, но разными Contact
    UUID sameId = UUID.randomUUID();
    Lead lead1 = new Lead(sameId, contact1, "CompanyA", "NEW");
    Lead lead2 = new Lead(sameId, contact2, "CompanyB", "QUALIFIED");

      // 4. Проверка на их равность
    assertThat(lead1).isEqualTo(lead2);
  }

  @Test
  void shouldThrowException_whenContactIsNull() {
    UUID id = UUID.randomUUID();

    assertThatThrownBy(() -> new Lead(id, null, "TestCorp", "NEW"))
              .isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("Contact cannot be null");
  }

  @Test
  void shouldThrowException_whenInvalidStatus() {
    Address address = new Address("San Francisco", "123 Main St", "94105");
    Contact contact = new Contact("John", "Doe", "john@example.com", "+71234", address);
    UUID id = UUID.randomUUID();

    assertThatThrownBy(() -> new Lead(id, contact, "TestCorp", "INVALID"))
              .isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("Status must be NEW, QUALIFIED or CONVERTED");
  }

  @Test
  void shouldDemonstrateThreeLevelComposition_whenAccessingCity() {
      // Создаем полную композицию
    Address address = new Address("San Francisco", "123 Main St", "94105");
    Contact contact = new Contact("John", "Doe", "john@example.com", "+71234", address);
    UUID id = UUID.randomUUID();
    Lead lead = new Lead(id, contact, "TestCorp", "NEW");

      // Демонстрируем трехуровневую делегацию (пошагово)
    Contact retrievedContact = lead.contact();        // уровень 1: получаем Contact
    Address retrievedAddress = retrievedContact.address(); // уровень 2: получаем Address
    String city = retrievedAddress.city();            // уровень 3: получаем city

      // Проверяем
    assertThat(city).isEqualTo("San Francisco");

      // Или сокращенно одной строкой
    String cityShort = lead.contact().address().city();
    assertThat(cityShort).isEqualTo("San Francisco");
  }
}