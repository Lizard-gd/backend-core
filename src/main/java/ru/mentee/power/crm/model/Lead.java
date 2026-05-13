package ru.mentee.power.crm.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "leads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lead {

  @Id
      @GeneratedValue(strategy = GenerationType.UUID)
      private UUID id;

  @Column(nullable = false)
      @NotBlank(message = "Имя обязательно")
      String firstName;

  @Column(nullable = false, unique = true, length = 255)
      @NotBlank(message = "Email обязателен")
      @Email(message = "Некорректный формат email")
      @Pattern(regexp = ".*\\..*", message = "Email должен содержать домен (например: .com, .ru)")
      String email;

  @Column(nullable = false)
      @NotBlank(message = "Телефон обязателен")
      @Pattern(regexp = "^\\+[0-9]{6,15}$",
              message = "Телефон должен начинаться с '+' и содержать от 6 до 15 цифр")
      String phone;

  @Column(nullable = false)
      @NotBlank(message = "Компания обязательна")
      String company;

  @Column(nullable = false)
      @NotBlank(message = "Статус обязателен")
      String status;

  @Column(name = "created_at", nullable = false, updatable = false)
      LocalDateTime createdAt;
}
