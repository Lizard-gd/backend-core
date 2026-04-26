package ru.mentee.power.crm.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import ru.mentee.power.crm.service.LeadService;

@SpringBootApplication
@ComponentScan(basePackages = {
    "ru.mentee.power.crm.service",
    "ru.mentee.power.crm.repository",
    "ru.mentee.power.crm.spring"})
public class Application {
  static void main(String[] args) {
    ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

    LeadService leadService = context.getBean(LeadService.class);

    if (leadService.findAll().isEmpty()) {
      leadService.addLead("John", "john@example.com", "+123456789", "Tech Corp", "NEW");
      leadService.addLead("Jane", "jane@example.com", "+987654321", "Design Studio", "CONTACTED");
      leadService.addLead("Ron", "ron@example.com", "+712345678", "Example Corp", "CONTACTED");
      leadService.addLead("Alex", "alex@example.com", "+134567890", "Prompt Studio", "NEW");
      leadService.addLead("Marty", "marty@example.com", "+987654321", "Merge Corp", "CONTACTED");
      leadService.addLead("Hacker", "<script>alert('XSS')</script>", "+999999999", "Hacker Corp", "NEW");
      leadService.addLead("Carl", "carl@example.com", "+204738503", "Test Corp", "CONTACTED");
      leadService.addLead("Paul", "paul@example.com", "+204759321", "Paul Studio", "QUALIFIED");
      leadService.addLead("Carol", "carol@example.com", "+345063218", "Form Corp", "QUALIFIED");
      leadService.addLead("Ralf", "ralf@example.com", "+098764987", "Nero Studio", "CONTACTED");
      leadService.addLead("Lost", "lost@example.com", "+000000000", "Lost Corp", "LOST");
    }
  }
}