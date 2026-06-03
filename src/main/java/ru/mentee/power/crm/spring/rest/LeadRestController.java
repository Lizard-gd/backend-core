package ru.mentee.power.crm.spring.rest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
  public ResponseEntity<List<Lead>> getAllLeads() {
    List<Lead> leads = leadService.findAll();
    return ResponseEntity.ok(leads);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Lead> getLeadById(@PathVariable UUID id) {
    Optional<Lead> leadOpt = leadService.findById(id);

    return leadOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<Lead> createLead(@RequestBody Lead lead) {
    Lead createdLead =
        leadService.addLead(
            lead.getFirstName(), lead.getEmail(), lead.getPhone(), lead.getStatus(), null);

    java.net.URI location = java.net.URI.create("/api/leads/" + createdLead.getId());

    return ResponseEntity.created(location).body(createdLead);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Lead> updateLead(@PathVariable UUID id, @RequestBody Lead lead) {
    Optional<Lead> updatedLeadOpt = leadService.updateLead(id, lead);

    return updatedLeadOpt
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteLead(@PathVariable UUID id) {
    boolean deleted = leadService.deleteLead(id);
    if (deleted) {
      return ResponseEntity.noContent().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}
