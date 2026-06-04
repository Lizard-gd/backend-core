package ru.mentee.power.crm.spring.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record LeadResponse(
    UUID id,
    String email,
    String firstName,
    String phone,
    String companyName,
    String status,
    LocalDateTime createdAt) {}
