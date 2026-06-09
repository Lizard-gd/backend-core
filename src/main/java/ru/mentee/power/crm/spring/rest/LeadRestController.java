package ru.mentee.power.crm.spring.rest;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
import ru.mentee.power.crm.spring.dto.CreateLeadRequest;
import ru.mentee.power.crm.spring.dto.LeadResponse;
import ru.mentee.power.crm.spring.dto.UpdateLeadRequest;
import ru.mentee.power.crm.spring.mapper.LeadMapper;

@RestController
@RequestMapping("/api/leads")
@Validated
public class LeadRestController {

  private final LeadService leadService;
  private final LeadMapper leadMapper;

  public LeadRestController(LeadService leadService, LeadMapper leadMapper) {
    this.leadService = leadService;
    this.leadMapper = leadMapper;
  }

  @GetMapping
  public ResponseEntity<List<LeadResponse>> getAllLeads() {
    List<Lead> leads = leadService.findAll();
    List<LeadResponse> responses = leads.stream().map(leadMapper::toResponse).toList();
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{id}")
  public ResponseEntity<LeadResponse> getLeadById(@PathVariable UUID id) {
    Lead lead = leadService.getLeadById(id);
    LeadResponse response = leadMapper.toResponse(lead);
    return ResponseEntity.ok(response);
  }

  @PostMapping
  public ResponseEntity<LeadResponse> createLead(@Valid @RequestBody CreateLeadRequest request) {
    Lead lead = leadMapper.toEntity(request);
    lead.setStatus("NEW");
    lead.setCreatedAt(LocalDateTime.now());

    Lead savedLead =
        leadService.addLead(
            lead.getFirstName(), lead.getEmail(), lead.getPhone(), lead.getStatus(), null);

    LeadResponse response = leadMapper.toResponse(savedLead);
    URI location = URI.create("/api/leads/" + savedLead.getId());
    return ResponseEntity.created(location).body(response);
  }

  @PutMapping("/{id}")
  public ResponseEntity<LeadResponse> updateLead(
      @PathVariable UUID id, @RequestBody UpdateLeadRequest request) {
    Lead existingLead = leadService.getLeadById(id);
    leadMapper.updateEntity(request, existingLead);
    Lead updatedLead = leadService.updateLeadOrThrow(id, existingLead);
    LeadResponse response = leadMapper.toResponse(updatedLead);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteLead(@PathVariable UUID id) {
    leadService.deleteLeadOrThrow(id);
    return ResponseEntity.noContent().build();
  }
}
