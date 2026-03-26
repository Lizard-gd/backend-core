package ru.mentee.power.crm.domain;

import lombok.Data;

@Data
public class ContactLombok {
  private final String firstName;
  private final String lastName;
  private final String email;
}
