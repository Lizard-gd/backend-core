package ru.mentee.power.crm.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

public class AddressTest {

  @Test
  void shouldCreateAddress_whenValidData() {
    Address address = new Address("San Francisco", "123 Main St", "94105");

    assertThat(address.city()).isEqualTo("San Francisco");
    assertThat(address.street()).isEqualTo("123 Main St");
    assertThat(address.zip()).isEqualTo("94105");
  }

  @Test
  void shouldBeEqual_whenSameData() {
    Address address1 = new Address("San Francisco", "123 Main St", "94105");
    Address address2 = new Address("San Francisco", "123 Main St", "94105");

    assertThat(address1).isEqualTo(address2);
    assertThat(address1.hashCode()).isEqualTo(address2.hashCode());
  }

  @Test
  void shouldThrowException_whenCityIsNull() {
    assertThatThrownBy(() -> new Address(null, "Street", "12345"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("City");
  }

  @Test
  void shouldThrowException_whenZipIsNull() {
    assertThatThrownBy(() -> new Address("City", "Street", ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Zip");
  }
}
