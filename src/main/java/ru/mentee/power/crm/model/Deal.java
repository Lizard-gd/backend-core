package ru.mentee.power.crm.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "deals")
@NoArgsConstructor
public class Deal {

  @Id
  @Column(name = "id", length = 36)
  private String id;

  @Column(name = "lead_id", nullable = false, length = 36)
  private String leadId;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private DealStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @OneToMany(mappedBy = "deal", cascade = CascadeType.ALL,
          orphanRemoval = true, fetch = FetchType.LAZY)
  private List<DealProduct> dealProducts = new ArrayList<>();

  public void addDealProduct(DealProduct dealProduct) {
    dealProducts.add(dealProduct);
    dealProduct.setDeal(this);
  }

  public void removeDealProduct(DealProduct dealProduct) {
    dealProducts.remove(dealProduct);
    dealProduct.setDeal(null);
  }

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

  public List<DealProduct> getDealProducts() {
    return dealProducts;
  }

  public void setDealProducts(List<DealProduct> dealProducts) {
    this.dealProducts = dealProducts;
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