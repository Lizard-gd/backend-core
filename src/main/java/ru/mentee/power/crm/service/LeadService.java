package ru.mentee.power.crm.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.repository.LeadRepository;

public class LeadService {

  private final LeadRepository repository;

  public LeadService(LeadRepository repository) {
    this.repository = repository;
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

  public Optional<Lead> findById(String id) {
    return repository.findById(id);
  }

  public  Optional<Lead> findByEmail(String email) {
    return repository.findByEmail(email);
  }
}
