package ru.mentee.power.crm.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LeadTest {

  @Test
void shouldReturnId_whenGetIdCalled() {
    UUID expectedId = UUID.randomUUID(); // сохраняем ID
    Lead lead = new Lead(expectedId, "test@example.com", "+71234567890", "TestCorp", "NEW");
    UUID actualId = lead.getId();
    assertThat(actualId).isEqualTo(expectedId); // сравниваем с сохранённым
  }

  @Test
void shouldReturnEmail_whenGetEmailCalled() {
    Lead lead = new Lead(UUID.randomUUID(), "test@example.com", "+71234567890", "TestCorp", "NEW");
    String email = lead.getEmail();
    assertThat(email).isEqualTo("test@example.com");
  }

  @Test
void shouldReturnPhone_whenGetPhoneCalled() {
    Lead lead = new Lead(UUID.randomUUID(), "test@example.com", "+71234567890", "TestCorp", "NEW");
    String phone = lead.getPhone();
    assertThat(phone).isEqualTo("+71234567890");
  }

  @Test
void shouldReturnCompany_whenGetCompanyCalled() {
    Lead lead = new Lead(UUID.randomUUID(), "test@example.com", "+71234567890", "TestCorp", "NEW");
    String company = lead.getCompany();
    assertThat(company).isEqualTo("TestCorp");
  }

  @Test
void shouldReturnStatus_whenGetStatusCalled() {
    Lead lead = new Lead(UUID.randomUUID(), "test@example.com", "+71234567890", "TestCorp", "NEW");
    String status = lead.getStatus();
    assertThat(status).isEqualTo("NEW");
  }

  @Test
void shouldReturnFormattedString_whenToStringCalled() {
    UUID id = UUID.randomUUID(); // сохраняем UUID
    Lead lead = new Lead(id, "test@example.com", "+71234567890", "TestCorp", "NEW");
    String result = lead.toString();
    assertThat(result).isEqualTo("Lead{id='" + id + "'"
            + ", email='test@example.com', phone='+71234567890', company='TestCorp', status='NEW'}");
  }

  @Test
void shouldPreventStringConfusion_whenUsingUUID() {
    UUID id = UUID.randomUUID();
    Lead lead = new Lead(id, "test@example.com", "+71234567890", "TestCorp", "NEW");

    class LeadService {
      Lead findByID(UUID id) {
        return null;
      }
    }
    LeadService service = new LeadService();
    service.findByID(lead.getId());
      // service.findById("some-string");  // ❌ Это НЕ скомпилируется
      // Демонстрация: если бы id был String, такой ошибки бы не было
      // и можно было бы случайно передать email или имя вместо id
    assertThat(lead.getId()).isEqualTo(id);
  }
}