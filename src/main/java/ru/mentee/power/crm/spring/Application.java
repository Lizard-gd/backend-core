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
      leadService.addLead("john@example.com", "+123456789", "Tech Corp", "NEW");
      leadService.addLead("jane@example.com", "+987654321", "Design Studio", "QUALIFIED");
      leadService.addLead("ron@example.com", "+712345678", "Example Corp", "QUALIFIED");
      leadService.addLead("alex@example.com", "+134567890", "Prompt Studio", "NEW");
      leadService.addLead("marty@example.com", "+987654321", "Merge Corp", "NEW");
      leadService.addLead("<script>alert('XSS')</script>", "+999999999", "Hacker Corp", "NEW");
    }
  }
}