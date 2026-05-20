package ru.mentee.power.crm.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "products")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @EqualsAndHashCode.Include
  private UUID id;

  @Column(nullable = false, length = 255)
  @EqualsAndHashCode.Include
  private String name;

  @Column(nullable = false, unique = true, length = 100)
  @EqualsAndHashCode.Include
  private String sku;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal price;

  @Column(nullable = false)
  private Boolean active = true;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @ToString.Exclude
  private List<DealProduct> dealProducts = new ArrayList<>();

  public void addDealProduct(DealProduct dealProduct) {
    dealProducts.add(dealProduct);
    dealProduct.setProduct(this);
  }

  public void removeDealProduct(DealProduct dealProduct) {
    dealProducts.remove(dealProduct);
    dealProduct.setProduct(null);
  }
}