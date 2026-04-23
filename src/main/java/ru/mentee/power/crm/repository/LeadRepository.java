package ru.mentee.power.crm.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import ru.mentee.power.crm.model.Lead;

@Repository
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

  public int size() {
    return storage.size();
  }

  public void delete(String id) {
    Lead removed = storage.remove(id);
    if (removed != null) {
      emailIndex.remove(removed.email());
    }
  }
}
