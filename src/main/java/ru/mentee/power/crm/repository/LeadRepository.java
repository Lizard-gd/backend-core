package ru.mentee.power.crm.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ru.mentee.power.crm.model.Lead;

public class LeadRepository {
  private final Map<String, Lead> storage = new HashMap<>();

  private final Map<String, String> emailIndex = new HashMap<>();

  public void save(Lead lead) {
    storage.put(lead.id(), lead);
    emailIndex.put(lead.email(), lead.id());
  }

  public Optional<Lead> findById(String id) {
    return Optional.ofNullable(storage.get(id));
  }

  public Optional<Lead> findByEmail(String email) {
    String id = emailIndex.get(email);
    return id == null ? Optional.empty() : Optional.ofNullable(storage.get(id));
  }

  public List<Lead> findAll() {
    return new ArrayList<>(storage.values());
  }

  public void delete(String id) {
    Lead lead = storage.remove(id);
    if (lead != null) {
      emailIndex.remove(lead.email());
    }
  }

  public int size() {
    return storage.size();
  }
}
