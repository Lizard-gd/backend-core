package ru.mentee.power.crm.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class LeadTest {

  @Test
void shouldReturnId_whenGetIdCalled() {
    Lead lead = new Lead("L1", "test@example.com", "+71234567890", "TestCorp", "NEW");
    String id = lead.getId();
    assertThat(id).isEqualTo("L1");
  }

  @Test
void shouldReturnEmail_whenGetEmailCalled() {
    Lead lead = new Lead("L1", "test@example.com", "+71234567890", "TestCorp", "NEW");
    String email = lead.getEmail();
    assertThat(email).isEqualTo("test@example.com");
  }

  @Test
void shouldReturnPhone_whenGetPhoneCalled() {
    Lead lead = new Lead("L1", "test@example.com", "+71234567890", "TestCorp", "NEW");
    String phone = lead.getPhone();
    assertThat(phone).isEqualTo("+71234567890");
  }

  @Test
void shouldReturnCompany_whenGetCompanyCalled() {
    Lead lead = new Lead("L1", "test@example.com", "+71234567890", "TestCorp", "NEW");
    String company = lead.getCompany();
    assertThat(company).isEqualTo("TestCorp");
  }

  @Test
void shouldReturnStatus_whenGetStatusCalled() {
    Lead lead = new Lead("L1", "test@example.com", "+71234567890", "TestCorp", "NEW");
    String status = lead.getStatus();
    assertThat(status).isEqualTo("NEW");
  }

  @Test
void shouldReturnFormattedString_whenToStringCalled() {
    Lead lead = new Lead("L1", "test@example.com", "+71234567890", "TestCorp", "NEW");
    String result = lead.toString();
    assertThat(result).isEqualTo("Lead{id='L1', "
            + "email='test@example.com', phone='+71234567890', company='TestCorp', status='NEW'}");
  }
}