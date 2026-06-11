package ru.mentee.power.crm.spring.rest;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.service.LeadService;
import ru.mentee.power.crm.spring.dto.generated.CreateLeadRequest;
import ru.mentee.power.crm.spring.dto.generated.LeadResponse;
import ru.mentee.power.crm.spring.dto.generated.UpdateLeadRequest;
import ru.mentee.power.crm.spring.mapper.LeadMapper;
import ru.mentee.power.crm.spring.rest.generated.LeadManagementApi;

@RestController
public class LeadRestController implements LeadManagementApi {

  private final LeadService leadService;
  private final LeadMapper leadMapper;

  public LeadRestController(LeadService leadService, LeadMapper leadMapper) {
    this.leadService = leadService;
    this.leadMapper = leadMapper;
  }

  @Override
  public ResponseEntity<List<LeadResponse>> getLeads() {
    List<Lead> leads = leadService.findAll();
    List<LeadResponse> responses = leads.stream().map(leadMapper::toResponse).toList();
    return ResponseEntity.ok(responses);
  }

  private void validateUpdateRequest(UpdateLeadRequest request) {
    if (request.getEmail() == null || request.getEmail().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
    }
    if (request.getFirstName() == null || request.getFirstName().length() < 2) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "First name must be at least 1 character");
    }
    if (request.getPhone() != null && !request.getPhone().matches("^\\+[0-9]{6,15}$")) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Phone must start with '+' and contain 6-15 digits");
    }
  }

  private void validateCreateRequest(CreateLeadRequest request) {
    if (request.getEmail() == null || request.getEmail().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
    }
    if (request.getFirstName() == null || request.getFirstName().length() < 2) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "First name must be at least 1 character");
    }
    if (request.getPhone() != null && !request.getPhone().matches("^\\+[0-9]{6,15}$")) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Phone must start with '+' and contain 6-15 digits");
    }
  }

  @Override
  public ResponseEntity<LeadResponse> createLead(CreateLeadRequest createLeadRequest) {
    validateCreateRequest(createLeadRequest);
    Lead lead = leadMapper.toEntity(createLeadRequest);
    lead.setStatus("NEW");
    lead.setCreatedAt(java.time.LocalDateTime.now());

    Lead saved =
        leadService.addLead(
            lead.getFirstName(), lead.getEmail(), lead.getPhone(), lead.getStatus(), null);

    LeadResponse response = leadMapper.toResponse(saved);
    URI location = URI.create("/api/leads/" + saved.getId());
    return ResponseEntity.created(location).body(response);
  }

  @Override
  public ResponseEntity<LeadResponse> getLeadById(UUID id) {
    Lead lead = leadService.getLeadById(id);
    LeadResponse response = leadMapper.toResponse(lead);
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<LeadResponse> updateLead(UUID id, UpdateLeadRequest updateLeadRequest) {
    validateUpdateRequest(updateLeadRequest);
    Lead existing = leadService.getLeadById(id);
    leadMapper.updateEntity(updateLeadRequest, existing);
    Lead updated = leadService.updateLeadOrThrow(id, existing);
    LeadResponse response = leadMapper.toResponse(updated);
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Void> deleteLead(UUID id) {
    leadService.getLeadById(id);
    leadService.deleteLeadOrThrow(id);
    return ResponseEntity.noContent().build();
  }
}
