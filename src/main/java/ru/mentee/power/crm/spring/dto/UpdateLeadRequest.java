package ru.mentee.power.crm.spring.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateLeadRequest {

  @NotBlank(message = "Email обязателен")
  @Email(message = "Некорректный формат email")
  private String email;

  @NotBlank(message = "Имя обязательно")
  @Size(max = 255, message = "Имя не должно превышать 255 символов")
  private String firstName;

  @NotBlank(message = "Телефон обязателен")
  @Size(max = 15, message = "Телефон не должен превышать 15 символов")
  private String phone;

  @Size(max = 255, message = "Название компании не должно превышать 255 символов")
  private String company;

  public UpdateLeadRequest() {

  }

  public UpdateLeadRequest(String email, String firstName, String phone, String company) {
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
