package ru.mentee.power.crm.spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.mentee.power.crm.repository.LeadRepository;
import ru.mentee.power.crm.service.LeadService;

@Controller
public class DemoController {
  private final LeadService constructorService;

  @Autowired
  private LeadRepository filedRepository;

  private LeadService setterService;

  public DemoController(LeadService constructorService) {
    this.constructorService = constructorService;
  }

  @Autowired(required = false)
  public void setSetterService(LeadService setterService) {
    this.setterService = setterService;
  }

  @GetMapping("/demo")
  @ResponseBody
  public String demo() {
    StringBuilder sb = new StringBuilder();
    sb.append("DI types Demo:\n\n");
    sb.append("Constructor Injection (final): ").append(constructorService != null ? "✓ Injected"
            : "✗ NULL").append("\n");
    sb.append("Filed Injection (@Autowired filed): ").append(filedRepository != null ? "✓ Injected"
            : "✗ NULL").append("\n");
    sb.append("Setter Injection (@Autowired setter): ").append(setterService != null ? "✓ Injected"
            : "✗ NULL").append("\n\n");
    sb.append("Recommendation: Use Constructor Injection with final fields.");
    return sb.toString().replace("\n", "<br>");
  }
}
