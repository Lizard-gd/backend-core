package ru.mentee.power.crm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "ru.mentee.power.crm.repository")
@EntityScan(basePackages = "ru.mentee.power.crm.model")
@ComponentScan(
    basePackages = {
      "ru.mentee.power.crm.service",
      "ru.mentee.power.crm.repository",
      "ru.mentee.power.crm.spring"
    })
public class Application {
  static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
