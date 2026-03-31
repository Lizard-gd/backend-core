package ru.mentee.power.crm.core;

import ru.mentee.power.crm.domain.Lead;

import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

public class LeadRepository {
  private final Set<Lead> leads = new HashSet<>();

  public boolean add(Lead lead) {
    return leads.add(lead);
  }

  public boolean contains(Lead lead) {
    return leads.contains(lead);
  }

  public Set<Lead> findAll() {
    return Collections.unmodifiableSet(leads);
  }

  public int size() {
    return leads.size();
  }
}