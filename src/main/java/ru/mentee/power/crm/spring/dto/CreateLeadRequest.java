package ru.mentee.power.crm.spring.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateLeadRequest {

  @NotBlank(message = "Email обязателен")
  @Email(message = "Некорректный формат email")
  private String email;

  @NotBlank(message = "Имя обязательно")
  @Size(min = 2, max = 50, message = "Имя должно быть от 2 до 50 символов")
  private String firstName;

  @NotBlank(message = "Телефон обязателен")
  @Size(max = 15, message = "Телефон не должен превышать 15 символов")
  @Pattern(
      regexp = "^\\+[0-9]{6,15}$",
      message = "Телефон должен начинаться с '+' и содержать 6-15 цифр")
  private String phone;

  @Size(max = 100, message = "Название компании не должно превышать 100 символов")
  private String company;

  public CreateLeadRequest() {}

  public CreateLeadRequest(String email, String firstName, String phone, String company) {
    this.email = email;
    this.firstName = firstName;
    this.phone = phone;
    this.company = company;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }
}
