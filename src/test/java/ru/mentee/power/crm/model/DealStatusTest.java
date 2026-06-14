package ru.mentee.power.crm.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DealStatusTest {

  @ParameterizedTest
  @CsvSource({
    "QUALIFIED, PAUSE, true",
    "PROPOSAL_SENT, PAUSE, true",
    "NEGOTIATION, PAUSE, true",
    "NEW, PAUSE, false",
    "WON, PAUSE, false",
    "LOST, PAUSE, false",
    "PAUSE, QUALIFIED, true",
    "PAUSE, PROPOSAL_SENT, true",
    "PAUSE, NEGOTIATION, true",
    "PAUSE, NEW, false",
    "PAUSE, WON, false",
    "PAUSE, LOST, false",
    "PAUSE, PAUSE, false"
  })
  void shouldValidateTransitionsWithPause(DealStatus from, DealStatus to, boolean expected) {
    assertThat(from.canTransitionTo(to)).isEqualTo(expected);
  }

  @Test
  void getAllowedTransitions_shouldReturnCorrectSetForPause() {
    Set<DealStatus> allowed = DealStatus.PAUSE.getAllowedTransitions();
    assertThat(allowed)
        .containsExactlyInAnyOrder(
            DealStatus.QUALIFIED, DealStatus.PROPOSAL_SENT, DealStatus.NEGOTIATION);
  }

  @Test
  void getAllowedTransitions_forTerminalStates_shouldReturnEmpty() {
    assertThat(DealStatus.WON.getAllowedTransitions()).isEmpty();
    assertThat(DealStatus.LOST.getAllowedTransitions()).isEmpty();
  }
}
