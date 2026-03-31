package ru.mentee.power.crm.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class CustomerTest {

  @Test
  void shouldReuseContact_whenCreatingCustomer() {

    Address address = new Address("San Francisco", "123 Main St", "94105");
    Contact contact = new Contact("John", "Doe", "john@example.com", "+71234", address);

    Address billingAddress = new Address("New York", "456 Broadway", "10001");

    UUID id = UUID.randomUUID();
    Customer customer = new Customer(id, contact, billingAddress, "BRONZE");

    assertThat(customer.contact().address()).isEqualTo(address);
    assertThat(customer.billingAddress()).isEqualTo(billingAddress);

    assertThat(customer.contact().address()).isNotEqualTo(customer.billingAddress());
  }

  @Test
  void shouldDemonstrateContactReuse_acrossLeadAndCustomer() {
    Address address = new Address("San Francisco", "123 Main St", "94105");
    Contact contact = new Contact("John", "Doe", "john@example.com", "+71234", address);

    UUID leadId = UUID.randomUUID();
    Lead lead = new Lead(leadId, contact, "TechCorp", "NEW");

    UUID customerId = UUID.randomUUID();
    Address billingAddress = new Address("New York", "456 Broadway", "10001");
    Customer customer = new Customer(customerId, contact, billingAddress, "BRONZE");

    assertThat(lead.contact()).isSameAs(customer.contact());

    assertThat(lead.contact().email()).isEqualTo("john@example.com");
    assertThat(customer.contact().email()).isEqualTo("john@example.com");
  }
}
