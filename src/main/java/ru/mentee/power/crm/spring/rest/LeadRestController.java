package ru.mentee.power.crm.spring.rest;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.service.LeadService;

@RestController
@RequestMapping("/api/leads")
public class LeadRestController {

  private final LeadService leadService;

  public LeadRestController(LeadService leadService) {
    this.leadService = leadService;
  }

  @GetMapping
  public List<Lead> getAllLeads() {
    return leadService.findAll();
  }

  @GetMapping("/{id}")
  public Lead getLeadById(@PathVariable UUID id) {
    return leadService.findById(id).orElse(null);
  }

  @PostMapping
  public Lead createLead(@RequestBody Lead lead) {

    return leadService.addLead(
        lead.getFirstName(), lead.getEmail(), lead.getPhone(), lead.getStatus(), null);
  }
}
