package ru.mentee.power.crm.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
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

  public Lead addLead(String email, String phone, String company, String status) {
    Optional<Lead> existing = repository.findByEmail(email);
    if (existing.isPresent()) {
      throw new IllegalStateException("Lead with email already exists: " + email);
    }

    String id = UUID.randomUUID().toString();
    Lead newLead = new Lead(id, email, phone, company, status);
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

  public  Optional<Lead> findByEmail(String email) {
    return repository.findByEmail(email);
  }
}
