package ru.mentee.power.crm.model;

import java.util.Map;
import java.util.Set;

public enum DealStatus {
  NEW,
  QUALIFIED,
  PROPOSAL_SENT,
  NEGOTIATION,
  WON,
  LOST,
  PAUSE;

  private static final Map<DealStatus, Set<DealStatus>> VALID_TRANSITIONS =
      Map.of(
          NEW, Set.of(QUALIFIED, LOST),
          QUALIFIED, Set.of(PROPOSAL_SENT, LOST, PAUSE),
          PROPOSAL_SENT, Set.of(NEGOTIATION, LOST, PAUSE),
          NEGOTIATION, Set.of(WON, LOST, PAUSE),
          PAUSE, Set.of(QUALIFIED, PROPOSAL_SENT, NEGOTIATION),
          WON, Set.of(),
          LOST, Set.of());

  public boolean canTransitionTo(DealStatus target) {
    Set<DealStatus> allowed = VALID_TRANSITIONS.get(this);
    return allowed != null && allowed.contains(target);
  }

  public Set<DealStatus> getAllowedTransitions() {
    return VALID_TRANSITIONS.getOrDefault(this, Set.of());
  }

  public String getCssClass() {
    return switch (this) {
      case LOST -> "bg-red-100 text-red-800";
      case NEW -> "bg-green-100 text-green-800";
      case QUALIFIED -> "bg-yellow-100 text-yellow-800";
      case PAUSE -> "bg-gray-300 text-gray-800";
      default -> "bg-blue-100 text-blue-800";
    };
  }
}
