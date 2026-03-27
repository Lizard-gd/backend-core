package ru.mentee.power.crm.domain;

public record Contact(String firstName, String lastName,
                      String email, String phone, Address address) {
  public Contact {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("Email cannot be null or blank");
    }
    if (phone == null || phone.isBlank()) {
      throw new IllegalArgumentException("Phone cannot be null or blank");
    }
    if (address == null) {
      throw new IllegalArgumentException("Address cannot be null");
    }
  }
}