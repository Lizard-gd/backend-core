package ru.mentee.power.crm.spring.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

public class FieldInjectionProblemTest {

  @Test
  void filedInjectionShowsNullWithoutSpring() {
    DemoController controller = new DemoController(null);

    assertThatCode(() -> controller.demo()).doesNotThrowAnyException();

    String result = controller.demo();
    assertThat(result)
        .contains("Constructor Injection (final): ✗ NULL")
        .contains("Filed Injection (@Autowired filed): ✗ NULL")
        .contains("Setter Injection (@Autowired setter): ✗ NULL");
  }
}
