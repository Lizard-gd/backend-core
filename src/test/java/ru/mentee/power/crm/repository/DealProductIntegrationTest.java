package ru.mentee.power.crm.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.power.crm.model.Deal;
import ru.mentee.power.crm.model.DealProduct;
import ru.mentee.power.crm.model.Product;

@SpringBootTest
@Transactional
class DealProductIntegrationTest {

  @Autowired private DealJpaRepository dealJpaRepository;

  @Autowired private ProductJpaRepository productJpaRepository;

  @Test
  void testSaveDealWithProducts() {
    Deal deal = new Deal(UUID.randomUUID().toString(), new BigDecimal("150000.00"));

    Product product1 = new Product();
    product1.setName("Ноутбук Dell XPS");
    product1.setSku("LAP-DELL-001");
    product1.setPrice(new BigDecimal("90000.00"));
    product1.setActive(true);

    Product product2 = new Product();
    product2.setName("Монитор LG");
    product2.setSku("MON-LG-002");
    product2.setPrice(new BigDecimal("25000.00"));
    product2.setActive(true);

    productJpaRepository.save(product1);
    productJpaRepository.save(product2);

    DealProduct dealProduct1 = new DealProduct(product1, 2, new BigDecimal("81000.00"));
    DealProduct dealProduct2 = new DealProduct(product2, 1, new BigDecimal("25000.00"));

    deal.addDealProduct(dealProduct1);
    deal.addDealProduct(dealProduct2);

    Deal savedDeal = dealJpaRepository.save(deal);

    dealJpaRepository.flush();
    // dealJpaRepository.deleteAll();
    Deal loadedDeal =
        dealJpaRepository
            .findDealWithProducts(savedDeal.getId())
            .orElseThrow(() -> new AssertionError("Deal not found"));

    assertThat(loadedDeal.getDealProducts()).hasSize(2);

    DealProduct loadedDp1 =
        loadedDeal.getDealProducts().stream()
            .filter(dp -> dp.getProduct().getSku().equals("LAP-DELL-001"))
            .findFirst()
            .orElseThrow();
    assertThat(loadedDp1.getQuantity()).isEqualTo(2);
    assertThat(loadedDp1.getUnitPrice()).isEqualByComparingTo("81000.00");

    DealProduct loadedDp2 =
        loadedDeal.getDealProducts().stream()
            .filter(dp -> dp.getProduct().getSku().equals("MON-LG-002"))
            .findFirst()
            .orElseThrow();
    assertThat(loadedDp2.getQuantity()).isEqualTo(1);
    assertThat(loadedDp2.getUnitPrice()).isEqualByComparingTo("25000.00");
  }

  @Autowired private jakarta.persistence.EntityManager entityManager;

  @Test
  void testEntityGraphSolvesNPlusOne() {

    Deal deal = new Deal(UUID.randomUUID().toString(), new BigDecimal("100000.00"));

    Product p1 = new Product();
    p1.setName("Product A");
    p1.setSku("SKU-A");
    p1.setPrice(new BigDecimal("100.00"));
    productJpaRepository.save(p1);

    Product p2 = new Product();
    p2.setName("Product B");
    p2.setSku("SKU-B");
    p2.setPrice(new BigDecimal("200.00"));
    productJpaRepository.save(p2);

    Product p3 = new Product();
    p3.setName("Product C");
    p3.setSku("SKU-C");
    p3.setPrice(new BigDecimal("300.00"));
    productJpaRepository.save(p3);

    productJpaRepository.flush();

    deal.addDealProduct(new DealProduct(p1, 1, new BigDecimal("100.00")));
    deal.addDealProduct(new DealProduct(p2, 2, new BigDecimal("200.00")));
    deal.addDealProduct(new DealProduct(p3, 3, new BigDecimal("300.00")));

    Deal saved = dealJpaRepository.save(deal);
    String dealId = saved.getId();

    dealJpaRepository.flush();

    entityManager.clear();

    Deal dealWithoutGraph = dealJpaRepository.findById(dealId).orElseThrow();
    assertThat(dealWithoutGraph.getDealProducts()).hasSize(3);

    for (DealProduct dp : dealWithoutGraph.getDealProducts()) {
      dp.getProduct().getName();
    }

    entityManager.clear();

    Deal dealWithGraph = dealJpaRepository.findDealWithProducts(dealId).orElseThrow();
    assertThat(dealWithGraph.getDealProducts()).hasSize(3);
  }
}
