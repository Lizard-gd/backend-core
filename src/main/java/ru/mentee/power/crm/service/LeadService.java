package ru.mentee.power.crm.service;

import java.time.LocalDateTime;
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

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
    Optional<Lead> existing = repository.findByEmailNative(email);
    if (existing.isPresent()) {
      throw new IllegalStateException("Lead with email already exists: " + email);
    }

    Lead newLead = new Lead();
    newLead.setFirstName(firstName);
    newLead.setEmail(email);
    newLead.setPhone(phone);
    newLead.setCompany(company);
    newLead.setStatus(status);
    newLead.setCreatedAt(LocalDateTime.now());
    repository.save(newLead);

    return newLead;
  }

  public List<Lead> findAll() {
    return repository.findAll();
  }

  public List<Lead> findByStatus(String status) {
    return repository.findByStatusNative(status);
  }

  public Optional<Lead> findById(UUID id) {
    return repository.findById(id);
  }

  public Optional<Lead> findByEmail(String email) {
    return repository.findByEmailNative(email);
  }

  public Lead update(UUID id, Lead updatedLead) {
    Optional<Lead> existing = repository.findById(id);
    if (existing.isEmpty()) {
      throw new IllegalArgumentException("Lead not found with id: " + id);
    }
    LocalDateTime originalCreatedAt = existing.get().getCreatedAt();
    Lead leadToSave = new Lead(id, updatedLead.getFirstName(), updatedLead.getEmail(),
            updatedLead.getPhone(), updatedLead.getCompany(),
            updatedLead.getStatus(), originalCreatedAt);
    repository.save(leadToSave);
    return leadToSave;
  }

  public void delete(UUID id) {
    Optional<Lead> existing = repository.findById(id);
    if (existing.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead not found with id: " + id);
    } repository.deleteById(id);
  }

  public List<Lead> findLeads(String search, String status,
                              LocalDateTime fromDateTime, LocalDateTime toDateTime) {
    List<Lead> allLeads = repository.findAll();

    var stream = allLeads.stream();

    if (search != null && !search.isBlank()) {
      String lowerSearch = search.toLowerCase();
      stream = stream.filter(lead ->
              lead.getFirstName().toLowerCase().contains(lowerSearch)
                      || lead.getEmail().toLowerCase().contains(lowerSearch)
      );
    }
    if (status != null && !status.isBlank()) {
      stream = stream.filter(lead -> lead.getStatus().equals(status));
    }
    if (fromDateTime != null) {
      stream = stream.filter(lead -> !lead.getCreatedAt().isBefore(fromDateTime));
    }
    if (toDateTime != null) {
      stream = stream.filter(lead -> !lead.getCreatedAt().isAfter(toDateTime));
    }
    return stream.collect(Collectors.toList());
  }

  public Optional<Lead> findByEmailDerived(String email) {
    return repository.findByEmail(email);
  }

  public List<Lead> findByStatuses(List<String> statuses) {
    return repository.findByStatusIn(statuses);
  }

  public Page<Lead> getLeadsByCompany(String company, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    return repository.findByCompany(company, pageable);
  }

  @Transactional
  public int bulkUpdateStatus(String oldStatus, String newStatus) {
    int updated = repository.updateStatusBulk(oldStatus, newStatus);
    System.out.println("Bulk update: " + updated + " leads changed from " + oldStatus + " to " + newStatus);
    return updated;
  }

  @Transactional
  public int bulkDeleteByStatus(String status) {
    int deleted = repository.deleteByStatusBulk(status);
    System.out.println("Bulk delete: " + deleted + " leads with status " + status + " removed");
    return deleted;
  }
}
