package ru.mentee.power.crm.model;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record Lead(
      String id,
      @NotBlank(message = "Имя обязательно")
      String firstName,
      @NotBlank(message = "Email обязателен")
      @Email(message = "Некорректный формат email")
      @Pattern(regexp = ".*\\..*", message = "Email должен содержать домен (например: .com, .ru)")
      String email,
      @NotBlank(message = "Телефон обязателен")
      @Pattern(regexp = "^\\+[0-9]{6,15}$",
              message = "Телефон должен начинаться с '+' и содержать от 6 до 15 цифр")
      String phone,
      @NotBlank(message = "Компания обязательна")
      String company,
      @NotBlank(message = "Статус обязателен")
      String status,
      LocalDateTime createdAt
) {

}
