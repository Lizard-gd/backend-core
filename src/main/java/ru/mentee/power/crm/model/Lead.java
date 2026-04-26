package ru.mentee.power.crm.model;

import java.time.LocalDateTime;

public record Lead(
      String id,
      String firstName,
      String email,
      String phone,
      String company,
      String status,
      LocalDateTime createdAt
) {

}
