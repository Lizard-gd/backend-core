package ru.mentee.power.crm.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ContactTest {

  @Test
  void shouldCreateContact_whenValidData() {
      // 1. Создаём объект
    Contact contact = new Contact("John", "Doe", "john@example.com");
      // 2. Проверяем, что геттеры возвращают правильные значения
    assertThat(contact.firstName()).isEqualTo("John");
    assertThat(contact.lastName()).isEqualTo("Doe");
    assertThat(contact.email()).isEqualTo("john@example.com");
  }

  @Test
  void shouldBeEqual_whenSameData() {
      // Создаём два контакта с одинаковыми данными
    Contact contact1 = new Contact("John", "Doe", "john@example.com");
    Contact contact2 = new Contact("John", "Doe", "john@example.com");
      // Проверяем, что они равны
    assertThat(contact1).isEqualTo(contact2);
      // Проверяем, что hashCode одинаковый
    assertThat(contact1.hashCode()).isEqualTo(contact2.hashCode());
  }

  @Test
  void shouldNotBeEqual_whenDifferentData() {
      // Создаём контакты с разными данными
    Contact contact1 = new Contact("John", "Doe", "john@example.com");
    Contact contact2 = new Contact("Jane", "Smith", "jane@example.com");

      // Проверяем, что они НЕ равны
    assertThat(contact1).isNotEqualTo(contact2);
  }
}