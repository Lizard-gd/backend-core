package ru.mentee.power.crm.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mentee.power.crm.domain.Address;
import ru.mentee.power.crm.domain.Contact;
import ru.mentee.power.crm.domain.Lead;

class LeadRepositoryTest {

  @Test
  @DisplayName("Should automatically deduplicate leads by id")
  void shouldDeduplicateLeadsById() {

    LeadRepository repository = new LeadRepository();
    UUID id = UUID.randomUUID();

    Address address = new Address("San Francisco", "123 Main St", "94105");
    Contact contact = new Contact("John", "Doe", "john@example.com", "+71234", address);
    Lead lead1 = new Lead(id, contact, "Company", "NEW");
    Lead lead2 = new Lead(id, contact, "Company", "NEW");

    boolean firstAdd = repository.add(lead1);
    boolean secondAdd = repository.add(lead2);

    assertThat(repository.size()).isEqualTo(1);
    assertThat(firstAdd).isTrue();
    assertThat(secondAdd).isFalse();
  }

  @Test
  @DisplayName("Should allow different leads with different ids")
  void shouldAllowDifferentLeads() {

    LeadRepository repository = new LeadRepository();

    Address address = new Address("San Francisco", "123 Main St", "94105");
    Contact contact = new Contact("John", "Doe", "john@example.com", "+71234", address);

    Lead lead1 = new Lead(UUID.randomUUID(), contact, "Company A", "NEW");
    Lead lead2 = new Lead(UUID.randomUUID(), contact, "Company B", "NEW");

    boolean firstAdd = repository.add(lead1);
    boolean secondAdd = repository.add(lead2);

    assertThat(repository.size()).isEqualTo(2);
    assertThat(firstAdd).isTrue();
    assertThat(secondAdd).isTrue();
  }

  @Test
  @DisplayName("Should find existing lead through contains")
  void shouldFindExistingLead() {

    LeadRepository repository = new LeadRepository();

    Address address = new Address("San Francisco", "123 Main St", "94105");
    Contact contact = new Contact("John", "Doe", "john@example.com", "+71234", address);

    Lead lead = new Lead(UUID.randomUUID(), contact, "Company", "NEW");

    repository.add(lead);

    assertThat(repository.contains(lead)).isTrue();

    Lead nonExistantLead = new Lead(UUID.randomUUID(), contact, "Other Company", "NEW");
    assertThat(repository.contains(nonExistantLead)).isFalse();
  }

  @Test
  @DisplayName("Should return unmodifiable set from findAll")
  void shouldReturnUnmodifiableSet() {
    LeadRepository repository = new LeadRepository();

    Address address = new Address("San Francisco", "123 Main St", "94105");
    Contact contact = new Contact("John", "Doe", "john@example.com", "+71234", address);

    Lead lead = new Lead(UUID.randomUUID(), contact, "Company", "NEW");

    repository.add(lead);

    Set<Lead> result = repository.findAll();

    assertThat(result).containsExactly(lead);
    assertThat(result).hasSize(1);

    assertThatThrownBy(() -> result.add(new Lead(UUID.randomUUID(), contact, "NEW", "NEW")))
          .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  @DisplayName("Should perform contains() faster then ArrayList")
  void shouldPerformFasterThenArrayList() {
    int size = 10_000;
    int iterations = 1_000;

    List<Lead> arrayList = new ArrayList<>();
    Set<Lead> hashSet = new HashSet<>();

    Address address = new Address("San Francisco", "123 Main St", "94105");
    Contact contact = new Contact("John", "Doe", "john@example.com", "+71234", address);

    List<Lead> allLeads = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      Lead lead = new Lead(UUID.randomUUID(), contact, "Company" + i, "NEW");
      allLeads.add(lead);
    }

    arrayList.addAll(allLeads);
    hashSet.addAll(allLeads);

    Lead targetLead = allLeads.get(size - 1);

    long arrayListStart = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
      arrayList.contains(targetLead);
    }
    long arrayListDuration = System.nanoTime() - arrayListStart;

    long hashSetStart = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
      hashSet.contains(targetLead);
    }
    long hashSetDuration = System.nanoTime() - hashSetStart;

    double speedUp = (double) arrayListDuration / hashSetDuration;

    System.out.println("ArrayList contains " + iterations + " times: "
            + arrayListDuration / 1_000_000.0 + " ms");
    System.out.println("HashSet contains " + iterations + " times: "
            + hashSetDuration / 1_000_000.0 + " ms");
    System.out.println("HashSet is " + String.format("%.2f", speedUp) + "x faster");

    assertThat(speedUp).isGreaterThan(100.0);
  }
}
