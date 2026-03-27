package ru.mentee.power.crm.domain;

import java.util.UUID;

public record Lead(UUID id, Contact contact, String company, String status) {
  public Lead {
    if (id == null) {
      throw new IllegalArgumentException("Id cannot be null");
    }
    if (contact == null) {
      throw new IllegalArgumentException("Contact cannot be null");
    }
    if (status == null) {
      throw new IllegalArgumentException("Status cannot be null");
    }
    if (!(status.equals("NEW") || status.equals("QUALIFIED") || status.equals("CONVERTED"))) {
      throw new IllegalArgumentException("Status must be NEW, QUALIFIED or CONVERTED");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Lead lead = (Lead) o;
    return id.equals(lead.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
