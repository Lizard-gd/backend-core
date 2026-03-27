package ru.mentee.power.crm.domain;

public record Address(String city, String street, String zip) {

  public Address {
    if (city == null || city.isBlank()) {
      throw new IllegalArgumentException("City cannot be null or blank!");
    }
    if (zip == null || zip.isBlank()) {
      throw new IllegalArgumentException("Zip cannot be null or blank");
    }
  }
}