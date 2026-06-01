package ru.mentee.power.crm.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import ru.mentee.power.crm.model.Product;

@DataJpaTest
@ActiveProfiles("test")
class ProductJpaRepositoryTest {

  @Autowired private ProductJpaRepository productRepository;

  @Test
  void shouldSaveAndFindProduct_whenValidData() {
    Product product = new Product();
    product.setName("Консультация");
    product.setSku("CONS-001");
    product.setPrice(new BigDecimal("5000.00"));
    product.setActive(true);

    Product saved = productRepository.save(product);
    assertThat(saved.getId()).isNotNull();

    Optional<Product> found = productRepository.findById(saved.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getSku()).isEqualTo("CONS-001");
  }

  @Test
  void shouldFindBySku_whenProductExists() {
    Product product = new Product();
    product.setName("Ноутбук");
    product.setSku("LAP-TOP-001");
    product.setPrice(new BigDecimal("120000.00"));
    productRepository.save(product);

    Optional<Product> found = productRepository.findBySku("LAP-TOP-001");
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Ноутбук");
  }

  @Test
  void shouldFindByActiveTrue_whenOnlyActiveProducts() {
    Product active1 = new Product();
    active1.setName("Активный1");
    active1.setSku("ACT1");
    active1.setPrice(BigDecimal.TEN);
    active1.setActive(true);
    productRepository.save(active1);

    Product active2 = new Product();
    active2.setName("Активный2");
    active2.setSku("ACT2");
    active2.setPrice(BigDecimal.ONE);
    active2.setActive(true);
    productRepository.save(active2);

    Product inactive = new Product();
    inactive.setName("Неактивный");
    inactive.setSku("INACT");
    inactive.setPrice(BigDecimal.ZERO);
    inactive.setActive(false);
    productRepository.save(inactive);

    List<Product> activeProducts = productRepository.findByActiveTrue();
    assertThat(activeProducts).hasSize(2);
    assertThat(activeProducts)
        .extracting(Product::getSku)
        .containsExactlyInAnyOrder("ACT1", "ACT2");
  }

  @Test
  void shouldThrow_whenDuplicateSku() {
    Product product1 = new Product();
    product1.setName("Товар 1");
    product1.setSku("DUPLICATE");
    product1.setPrice(new BigDecimal("100"));
    productRepository.save(product1);

    Product product2 = new Product();
    product2.setName("Товар 2");
    product2.setSku("DUPLICATE");
    product2.setPrice(new BigDecimal("200"));

    assertThatThrownBy(
            () -> {
              productRepository.save(product2);
              productRepository.flush();
            })
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
