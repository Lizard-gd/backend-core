package ru.mentee.power.crm.service;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.repository.LeadRepository;

@Service
public class LeadService {

  private static final Logger log = LoggerFactory.getLogger(LeadService.class);
  private final LeadRepository repository;

  public LeadService(LeadRepository repository) {
    this.repository = repository;
    log.info("LeadService constructor called");
  }

  @PostConstruct
  void init() {
    log.info("LeadService @PostConstruct init() called - Bean lifecycle phase");
  }

  public Lead addLead(String firstName, String email, String phone, String company, String status) {
    Optional<Lead> existing = repository.findByEmail(email);
    if (existing.isPresent()) {
      throw new IllegalStateException("Lead with email already exists: " + email);
    }

    String id = UUID.randomUUID().toString();
    LocalDateTime now = LocalDateTime.now();
    Lead newLead = new Lead(id, firstName, email, phone, company, status, now);
    repository.save(newLead);

    return newLead;
  }

  public List<Lead> findAll() {
    return repository.findAll();
  }

  public List<Lead> findByStatus(String status) {
    return repository.findAll().stream()
            .filter(lead -> lead.status().equals(status))
            .collect(Collectors.toList());
  }

  public Optional<Lead> findById(String id) {
    return repository.findById(id);
  }

  public Optional<Lead> findByEmail(String email) {
    return repository.findByEmail(email);
  }

  public Lead update(String id, Lead updatedLead) {
    Optional<Lead> existing = repository.findById(id);
    if (existing.isEmpty()) {
      throw new IllegalArgumentException("Lead not found with id: " + id);
    }
    LocalDateTime originalCreatedAt = existing.get().createdAt();
    Lead leadToSave = new Lead(id, updatedLead.firstName(), updatedLead.email(), updatedLead.phone(),
            updatedLead.company(), updatedLead.status(), originalCreatedAt);
    repository.save(leadToSave);
    return leadToSave;
  }

  public void delete(String id) {
    Optional<Lead> existing = repository.findById(id);
    if (existing.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead not found with id: " + id);
    } repository.delete(id);
  }

  public List<Lead> findLeads(String search, String status, LocalDateTime fromDateTime, LocalDateTime toDateTime) {
    List<Lead> allLeads = repository.findAll();

    var stream = allLeads.stream();

    if (search != null && !search.isBlank()) {
      String lowerSearch = search.toLowerCase();
      stream = stream.filter(lead ->
              lead.firstName().toLowerCase().contains(lowerSearch) ||
                      lead.email().toLowerCase().contains(lowerSearch)
      );
    }
    if (status != null && !status.isBlank()) {
      stream = stream.filter(lead -> lead.status().equals(status));
    }
    if (fromDateTime != null) {
      stream = stream.filter(lead -> !lead.createdAt().isBefore(fromDateTime));
    }
    if (toDateTime != null) {
      stream = stream.filter(lead -> !lead.createdAt().isAfter(toDateTime));
    }
    return stream.collect(Collectors.toList());
  }
}
