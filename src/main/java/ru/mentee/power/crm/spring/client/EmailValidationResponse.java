package ru.mentee.power.crm.spring.client;

public record EmailValidationResponse(String email, boolean valid, String reason) {}
