package ru.mentee.power.crm.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class LeadEqualsHashCodeTest {

  private static final UUID TEST_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID OTHER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Test
    void shouldBeReflexive_whenEqualsCalledOnSameObject() {
      // Given: создаем лида с любыми данными
    Address address = new Address("City", "Street", "Zip");
    Contact contact = new Contact("Ivan", "Ivanov", "ivan@mail.ru", "+7123", address);
    Lead lead = new Lead(TEST_ID, contact, "TechCorp", "NEW");
      // Then: проверяем, что лид равен сам себе
    assertThat(lead).isEqualTo(lead);
  }

  @Test
  void shouldBeSymmetric_whenEqualsCalledOnTwoObjects() {
      // Given: Даём на проверку с первым второй лид
    Address address = new Address("City", "Street", "Zip");
    Contact contact = new Contact("Ivan", "Ivanov", "ivan@mail.ru", "+7123", address);
    Lead firstLead = new Lead(TEST_ID, contact, "TechCorp", "NEW");
    Lead secondLead = new Lead(TEST_ID, contact, "TechCorp", "NEW");
      // Then: Симметричность — порядок сравнения не важен
    assertThat(firstLead).isEqualTo(secondLead);
    assertThat(secondLead).isEqualTo(firstLead);
  }

  @Test
  void shouldBeTransitive_whenEqualsChainOfThreeObjects() {
    Address address = new Address("City", "Street", "Zip");
    Contact contact = new Contact("Ivan", "Ivanov", "ivan@mail.ru", "+7123", address);

    Lead firstLead = new Lead(TEST_ID, contact, "TechCorp", "NEW");
    Lead secondLead = new Lead(TEST_ID, contact, "TechCorp", "NEW");
    Lead thirdLead = new Lead(TEST_ID, contact, "TechCorp", "NEW");

      // Then: Транзитивность — если A=B и B=C, то A=C
    assertThat(firstLead).isEqualTo(secondLead);
    assertThat(secondLead).isEqualTo(thirdLead);
    assertThat(firstLead).isEqualTo(thirdLead);
  }

  @Test
  void shouldBeConsistent_whenEqualsCalledMultipleTimes() {
    Address address = new Address("City", "Street", "Zip");
    Contact contact = new Contact("Ivan", "Ivanov", "ivan@mail.ru", "+7123", address);

    Lead firstLead = new Lead(TEST_ID, contact, "TechCorp", "NEW");
    Lead secondLead = new Lead(TEST_ID, contact, "TechCorp", "NEW");

      // Then: Результат одинаковый при многократных вызовах
    assertThat(firstLead).isEqualTo(secondLead);
    assertThat(firstLead).isEqualTo(secondLead);
    assertThat(firstLead).isEqualTo(secondLead);
  }

  @Test
  void shouldReturnFalse_whenEqualsComparedWithNull() {
    Address address = new Address("City", "Street", "Zip");
    Contact contact = new Contact("Ivan", "Ivanov", "ivan@mail.ru", "+7123", address);

    Lead lead = new Lead(TEST_ID, contact, "TechCorp", "NEW");

      // Then: Объект не равен null (isNotEqualTo проверяет equals(null) = false)
    assertThat(lead).isNotEqualTo(null);
  }

  @Test
  void shouldHaveSameHashCode_whenObjectsAreEqual() {
    Address address = new Address("City", "Street", "Zip");
    Contact contact = new Contact("Ivan", "Ivanov", "ivan@mail.ru", "+7123", address);

    Lead firstLead = new Lead(TEST_ID, contact, "TechCorp", "NEW");
    Lead secondLead = new Lead(TEST_ID, contact, "TechCorp", "NEW");

      // Then: Если объекты равны, то hashCode должен быть одинаковым
    assertThat(firstLead).isEqualTo(secondLead);
    assertThat(firstLead.hashCode()).isEqualTo(secondLead.hashCode());
  }

  @Test
  void shouldWorkInHashMap_whenLeadUsedAsKey() {
    Address address = new Address("City", "Street", "Zip");
    Contact contact = new Contact("Ivan", "Ivanov", "ivan@mail.ru", "+7123", address);

    Lead keyLead = new Lead(TEST_ID, contact, "TechCorp", "NEW");
    Lead lookupLead = new Lead(TEST_ID, contact, "TechCorp", "NEW");

    Map<Lead, String> map = new HashMap<>();
    map.put(keyLead, "CONTACTED");

      // When: Получаем значение по другому объекту с тем же id
    String status = map.get(lookupLead);

      // Then: HashMap нашел значение благодаря equals/hashCode
    assertThat(status).isEqualTo("CONTACTED");
  }

  @Test
  void shouldNotBeEqual_whenIdsAreDifferent() {
    Address address = new Address("City", "Street", "Zip");
    Contact contact = new Contact("Ivan", "Ivanov", "ivan@mail.ru", "+7123", address);

    Lead firstLead = new Lead(TEST_ID, contact, "TechCorp", "NEW");
    Lead differentLead = new Lead(OTHER_ID, contact, "TechCorp", "NEW");

      // Then: Разные id = разные объекты (isNotEqualTo использует equals() внутри)
    assertThat(firstLead).isNotEqualTo(differentLead);
  }

  @Test
  void shouldBeEqual_whenOnlyIdMatches_otherFieldsCanBeDifferent() {
    Address address = new Address("City", "Street", "Zip");
    Contact contact = new Contact("Ivan", "Ivanov", "ivan@mail.ru", "+7123", address);

    Lead firstLead = new Lead(TEST_ID, contact, "TechCorp", "NEW");
    Lead secondLead = new Lead(TEST_ID, contact, "CorpPork", "QUALIFIED");

    assertThat(firstLead).isEqualTo(secondLead);
    assertThat(firstLead.hashCode()).isEqualTo(secondLead.hashCode());
  }
}