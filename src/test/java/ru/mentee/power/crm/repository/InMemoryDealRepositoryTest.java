package ru.mentee.power.crm.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mentee.power.crm.model.Deal;
import ru.mentee.power.crm.model.DealStatus;

class InMemoryDealRepositoryTest {

  private InMemoryDealRepository repository;

  @BeforeEach
  void setUp() {
    repository = new InMemoryDealRepository();
  }

  @Test
  void shouldSaveAndRetrieveDeal() {
    Deal deal = new Deal("lead-1", new BigDecimal("1000.00"));
    repository.save(deal);

    Optional<Deal> found = repository.findById(deal.getId());
    assertThat(found).isPresent();
    assertThat(found.get()).isEqualTo(deal);
  }

  @Test
  void shouldUpdateExistingDeal() {
    Deal deal = new Deal("lead-1", new BigDecimal("500.00"));
    repository.save(deal);

    deal.transitionTo(DealStatus.QUALIFIED);
    repository.save(deal);

    Optional<Deal> found = repository.findById(deal.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getStatus()).isEqualTo(DealStatus.QUALIFIED);
  }

  @Test
  void shouldReturnEmptyOptionalWhenNotFound() {
    Optional<Deal> found = repository.findById("non-existent-id");
    assertThat(found).isEmpty();
  }

  @Test
  void shouldFindAllDeals() {
    Deal deal1 = new Deal("lead-1", new BigDecimal("100"));
    Deal deal2 = new Deal("lead-2", new BigDecimal("200"));
    repository.save(deal1);
    repository.save(deal2);

    List<Deal> all = repository.findAll();
    assertThat(all).hasSize(2).containsExactlyInAnyOrder(deal1, deal2);
  }

  @Test
  void findAllShouldReturnEmptyListWhenNoDeals() {
    List<Deal> all = repository.findAll();
    assertThat(all).isEmpty();
  }

  @Test
  void shouldFindByStatus() {
    Deal newDeal = new Deal("lead-1", new BigDecimal("100"));

    Deal wonDeal = new Deal("lead-2", new BigDecimal("200"));

    wonDeal.transitionTo(DealStatus.QUALIFIED);
    wonDeal.transitionTo(DealStatus.PROPOSAL_SENT);
    wonDeal.transitionTo(DealStatus.NEGOTIATION);
    wonDeal.transitionTo(DealStatus.WON);

    repository.save(newDeal);
    repository.save(wonDeal);

    List<Deal> newDeals = repository.findByStatus(DealStatus.NEW);
    List<Deal> wonDeals = repository.findByStatus(DealStatus.WON);

    assertThat(newDeals).containsExactly(newDeal);
    assertThat(wonDeals).containsExactly(wonDeal);
  }

  @Test
  void findByStatusShouldReturnEmptyListWhenNoMatching() {
    Deal deal = new Deal("lead-1", new BigDecimal("100"));
    repository.save(deal);

    List<Deal> lostDeals = repository.findByStatus(DealStatus.LOST);
    assertThat(lostDeals).isEmpty();
  }

  @Test
  void shouldDeleteById() {
    Deal deal = new Deal("lead-1", new BigDecimal("100"));
    repository.save(deal);
    assertThat(repository.findById(deal.getId())).isPresent();

    repository.deleteById(deal.getId());
    assertThat(repository.findById(deal.getId())).isEmpty();
  }

  @Test
  void deleteByIdShouldNotFailWhenIdDoesNotExist() {
    assertThatCode(() -> repository.deleteById("non-existent"))
        .doesNotThrowAnyException();
  }
}