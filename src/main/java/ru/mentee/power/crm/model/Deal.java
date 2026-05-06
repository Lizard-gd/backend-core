package ru.mentee.power.crm.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Deal {
  private final String id;
  private final String leadId;
  private final BigDecimal amount;
  private DealStatus status;
  private final LocalDateTime createdAt;

  public Deal(String leadId, BigDecimal amount) {
    this.id = UUID.randomUUID().toString();
    this.leadId = Objects.requireNonNull(leadId, "leadId must not be null");
    this.amount = Objects.requireNonNull(amount, "amount must not be null");
    this.status = DealStatus.NEW;
    this.createdAt = LocalDateTime.now();
  }

  public Deal(String id, String leadId, BigDecimal amount,
              DealStatus status, LocalDateTime createdAt) {
    this.id = id;
    this.leadId = leadId;
    this.amount = amount;
    this.status = status;
    this.createdAt = createdAt;
  }

  public void transitionTo(DealStatus newStatus) {
    if (!status.canTransitionTo(newStatus)) {
      throw new IllegalStateException("Cannot transition from " + status + " to " + newStatus);
    }
    this.status = newStatus;
  }

  public String getId() {
    return id;
  }

  public String getLeadId() {
    return leadId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public DealStatus getStatus() {
    return status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Deal deal = (Deal) o;
    return Objects.equals(id, deal.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}