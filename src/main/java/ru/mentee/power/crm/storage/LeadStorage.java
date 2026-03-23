package ru.mentee.power.crm.storage;

import ru.mentee.power.crm.domain.Lead;

public class LeadStorage {
  private Lead[] leads = new Lead[100];

  public boolean add (Lead lead) {
    for (int i = 0; i < leads.length; i++) {
      if (leads[i] != null && leads[i].getEmail().equals(lead.getEmail())) {
        return false;
      }
      if (leads[i] == null) {
        leads[i] = lead;
        return true;
      }
    }
    throw new IllegalStateException("Storage is full");
  }

  public Lead[] findAll() {
    int count = 0;
    for (int i = 0; i < leads.length; i++) {
      if (leads[i] != null) {
        count++;
      }
    }
    Lead[] result = new Lead[count];
    int index = 0;
    for (int i = 0; i < leads.length; i++) {
      if (leads[i] != null) {
        result[index] = leads[i];
        index++;
      }
    }
    return result;
  }

  public int size() {
    int size = 0;
    for (int s = 0; s < leads.length; s++) {
      if (leads[s] != null) {
        size++;
      }
    }
    return size;
  }
}