package ru.mentee.power.crm.spring.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

//import org.junit.jupiter.api.Test;

public class FieldInjectionProblemTest {

 // @Test
  void filedInjectionCausesNullPointerWithoutSpring() {
      // Демонстрация: Field Injection не работает без Spring контейнера
    DemoController controller = new DemoController(null);
      // Поле fieldRepository осталось null – при вызове demo() будет NPE
    assertThatThrownBy(() -> controller.demo())
            .isInstanceOf(NullPointerException.class);
  }
}
