package ru.mentee.power.crm.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DealTest {

  @Test
  void shouldCreateDeal_withNewStatus() {
    String leadId = UUID.randomUUID().toString();
    BigDecimal amount = new BigDecimal("100000.00");

    Deal deal = new Deal(leadId, amount);

    assertThat(deal.getId()).isNotNull();
    assertThat(deal.getLeadId()).isEqualTo(leadId);
    assertThat(deal.getAmount()).isEqualTo(amount);
    assertThat(deal.getStatus()).isEqualTo(DealStatus.NEW);
    assertThat(deal.getCreatedAt()).isNotNull();
  }

  @Test
  void shouldTransitionToValidStatus() {
    Deal deal = new Deal("lead-123", BigDecimal.valueOf(50000));
    deal.transitionTo(DealStatus.QUALIFIED);
    assertThat(deal.getStatus()).isEqualTo(DealStatus.QUALIFIED);

    deal.transitionTo(DealStatus.PROPOSAL_SENT);
    assertThat(deal.getStatus()).isEqualTo(DealStatus.PROPOSAL_SENT);
  }

  @Test
  void shouldThrowException_whenTransitionInvalid() {
    Deal deal = new Deal("lead-123", BigDecimal.valueOf(50000));
    deal.transitionTo(DealStatus.LOST);

    assertThatThrownBy(() -> deal.transitionTo(DealStatus.NEW))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot transition from LOST to NEW");
  }

  @Test
  void shouldCreateDealFromConstructor_withAllFields() {
    String id = "deal-123";
    String leadId = "lead-456";
    BigDecimal amount = new BigDecimal("999.99");
    DealStatus status = DealStatus.NEGOTIATION;
    var createdAt = java.time.LocalDateTime.now();

    Deal deal = new Deal(id, leadId, amount, status, createdAt);

    assertThat(deal.getId()).isEqualTo(id);
    assertThat(deal.getLeadId()).isEqualTo(leadId);
    assertThat(deal.getAmount()).isEqualTo(amount);
    assertThat(deal.getStatus()).isEqualTo(status);
    assertThat(deal.getCreatedAt()).isEqualTo(createdAt);
  }

  @Test
  void equalsAndHashCode_shouldWorkById() {
    Deal deal1 = new Deal("lead-1", BigDecimal.TEN);
    Deal deal2 =
        new Deal(deal1.getId(), "lead-2", BigDecimal.ONE, DealStatus.NEW, deal1.getCreatedAt());

    assertThat(deal1).isEqualTo(deal2);
    assertThat(deal1.hashCode()).isEqualTo(deal2.hashCode());
  }
}
