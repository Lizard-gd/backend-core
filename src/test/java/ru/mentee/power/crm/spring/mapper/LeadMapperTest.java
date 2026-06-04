package ru.mentee.power.crm.spring.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.spring.dto.CreateLeadRequest;
import ru.mentee.power.crm.spring.dto.LeadResponse;
import ru.mentee.power.crm.spring.dto.UpdateLeadRequest;

@SpringBootTest
class LeadMapperTest {

  @Autowired private LeadMapper leadMapper;

  @Test
  void toEntity_shouldMapCreateRequestToLead_ignoringIdAndTimestampsAndStatus() {
    CreateLeadRequest request =
        new CreateLeadRequest("test@example.com", "John", "+123456789", "ACME");

    Lead lead = leadMapper.toEntity(request);

    assertThat(lead).isNotNull();
    assertThat(lead.getId()).isNull();
    assertThat(lead.getCreatedAt()).isNull();
    assertThat(lead.getVersion()).isNull();
    assertThat(lead.getStatus()).isNull();
    assertThat(lead.getCompany()).isNull();
    assertThat(lead.getEmail()).isEqualTo("test@example.com");
    assertThat(lead.getFirstName()).isEqualTo("John");
    assertThat(lead.getPhone()).isEqualTo("+123456789");
  }

  @Test
  void toResponse_shouldMapLeadToLeadResponse() {
    UUID id = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();
    Lead lead = new Lead();
    lead.setId(id);
    lead.setEmail("response@example.com");
    lead.setFirstName("Jane");
    lead.setPhone("+987654321");
    lead.setStatus("QUALIFIED");
    lead.setCreatedAt(now);

    LeadResponse response = leadMapper.toResponse(lead);

    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(id);
    assertThat(response.email()).isEqualTo("response@example.com");
    assertThat(response.firstName()).isEqualTo("Jane");
    assertThat(response.phone()).isEqualTo("+987654321");
    assertThat(response.status()).isEqualTo("QUALIFIED");
    assertThat(response.createdAt()).isEqualTo(now);
    assertThat(response.companyName()).isNull();
  }

  @Test
  void updateEntity_shouldUpdateLeadFieldsFromUpdateRequest_ignoringIdAndStatus() {
    Lead existingLead = new Lead();
    existingLead.setId(UUID.randomUUID());
    existingLead.setEmail("old@example.com");
    existingLead.setFirstName("Old");
    existingLead.setPhone("+000");
    existingLead.setStatus("NEW");
    existingLead.setCreatedAt(LocalDateTime.now().minusDays(1));

    UpdateLeadRequest request =
        new UpdateLeadRequest("newemail@example.com", "NewName", "+111", "UpdatedCompany");

    leadMapper.updateEntity(request, existingLead);

    assertThat(existingLead.getId()).isNotNull();
    assertThat(existingLead.getCreatedAt()).isNotNull();
    assertThat(existingLead.getStatus()).isEqualTo("NEW");
    assertThat(existingLead.getEmail()).isEqualTo("newemail@example.com");
    assertThat(existingLead.getFirstName()).isEqualTo("NewName");
    assertThat(existingLead.getPhone()).isEqualTo("+111");
    assertThat(existingLead.getCompany()).isNull();
  }
}
