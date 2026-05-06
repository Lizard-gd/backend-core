package ru.mentee.power.crm.model;

import java.util.Map;
import java.util.Set;

public enum DealStatus {
  NEW,
  QUALIFIED,
  PROPOSAL_SENT,
  NEGOTIATION,
  WON,
  LOST;

  private static final Map<DealStatus, Set<DealStatus>> VALID_TRANSITIONS = Map.of(
        NEW, Set.of(QUALIFIED, LOST),
        QUALIFIED, Set.of(PROPOSAL_SENT, LOST),
        PROPOSAL_SENT, Set.of(NEGOTIATION, LOST),
        NEGOTIATION, Set.of(WON, LOST),
        WON, Set.of(),
        LOST, Set.of()
  );

  public boolean canTransitionTo(DealStatus target) {
    Set<DealStatus> allowed = VALID_TRANSITIONS.get(this);
    return allowed != null && allowed.contains(target);
  }
}
