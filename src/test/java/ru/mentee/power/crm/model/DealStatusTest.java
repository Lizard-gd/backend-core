package ru.mentee.power.crm.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DealStatusTest {

  @ParameterizedTest
  @CsvSource({
      "NEW, QUALIFIED, true",
      "NEW, LOST, true",
      "NEW, PROPOSAL_SENT, false",
      "NEW, WON, false",
      "QUALIFIED, PROPOSAL_SENT, true",
      "QUALIFIED, LOST, true",
      "QUALIFIED, WON, false",
      "PROPOSAL_SENT, NEGOTIATION, true",
      "PROPOSAL_SENT, LOST, true",
      "PROPOSAL_SENT, WON, false",
      "NEGOTIATION, WON, true",
      "NEGOTIATION, LOST, true",
      "NEGOTIATION, QUALIFIED, false",
      "WON, NEW, false",
      "WON, LOST, false",
      "LOST, QUALIFIED, false"
  })
  void shouldValidateTransitions(DealStatus from, DealStatus to, boolean expected) {
    assertThat(from.canTransitionTo(to)).isEqualTo(expected);
  }

  @Test
  void terminalStates_shouldNotAllowAnyTransitions() {
    for (DealStatus terminal : new DealStatus[]{DealStatus.WON, DealStatus.LOST}) {
      for (DealStatus target : DealStatus.values()) {
        assertThat(terminal.canTransitionTo(target))
                      .as("Checking %s -> %s", terminal, target)
                      .isFalse();
      }
    }
  }
}