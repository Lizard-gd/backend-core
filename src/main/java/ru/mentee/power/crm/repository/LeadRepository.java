package ru.mentee.power.crm.repository;

import ru.mentee.power.crm.model.Lead;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeadRepository {
  private final Map<String, Lead> storage = new HashMap<>();

  public void save(Lead lead) {
    storage.put(lead.id(), lead);
  }

  public Lead findById(String id) {
    return storage.get(id);
  }

  public List<Lead> findAll() {
    return new ArrayList<>(storage.values());
  }

  public void delete(String id) {
    storage.remove(id);
  }

  public int size() {
    return storage.size();
  }
}
