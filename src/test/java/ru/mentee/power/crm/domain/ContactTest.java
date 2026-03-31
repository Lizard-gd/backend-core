package ru.mentee.power.crm.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

public class ContactTest {

  @Test
  void shouldCreateContact_whenValidData() {
      // 1. Создаём объект
    Address address = new Address("San Francisco", "123 Main St", "94105");
    Contact contact = new Contact("John", "Doe", "john@example.com", "+71234", address);

      // 2. Проверяем, что геттеры возвращают правильные значения
    assertThat(contact.firstName()).isEqualTo("John");
    assertThat(contact.lastName()).isEqualTo("Doe");
    assertThat(contact.email()).isEqualTo("john@example.com");
    assertThat(contact.phone()).isEqualTo("+71234");

    assertThat(contact.address()).isEqualTo(address);
    assertThat(address.city()).isEqualTo("San Francisco");
  }

  @Test
  void shouldDelegateToAddress_whenAccessingCity() {
    Address address = new Address("San Francisco", "123 Main St", "94105");
    Contact contact = new Contact("John", "Doe", "john@example.com", "+71234", address);

    assertThat(contact.address().city()).isEqualTo("San Francisco");
    assertThat(contact.address().street()).isEqualTo("123 Main St");
  }

  @Test
  void shouldThrowException_whenAddressIsNull() {
    assertThatThrownBy(() -> new Contact("John", "Doe", "john@example.com", "+71234", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Address");
  }

  @Test
  void shouldBeEqual_whenSameData() {
    Address address = new Address("San Francisco", "123 Main St", "94105");
      // Создаём два контакта с одинаковыми данными
    Contact contact1 = new Contact("John", "Doe", "john@example.com", "+71234", address);
    Contact contact2 = new Contact("John", "Doe", "john@example.com", "+71234", address);
      // Проверяем, что они равны
    assertThat(contact1).isEqualTo(contact2);
      // Проверяем, что hashCode одинаковый
    assertThat(contact1.hashCode()).isEqualTo(contact2.hashCode());
  }

  @Test
  void shouldNotBeEqual_whenDifferentData() {
    Address address = new Address("San Francisco", "123 Main St", "94105");
      // Создаём контакты с разными данными
    Contact contact1 = new Contact("John", "Doe", "john@example.com", "+71234", address);
    Contact contact2 = new Contact("Jane", "Smith", "jane@example.com", "+71234", address);

      // Проверяем, что они НЕ равны
    assertThat(contact1).isNotEqualTo(contact2);
  }
}