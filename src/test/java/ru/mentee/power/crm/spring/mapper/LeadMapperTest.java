package ru.mentee.power.crm.spring.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.spring.dto.generated.CreateLeadRequest;
import ru.mentee.power.crm.spring.dto.generated.LeadResponse;
import ru.mentee.power.crm.spring.dto.generated.UpdateLeadRequest;

@SpringBootTest
class LeadMapperTest {

  @Autowired private LeadMapper leadMapper;

  @Test
  void toEntity_shouldMapCreateRequestToLead_ignoringIdAndTimestampsAndStatus() {
    CreateLeadRequest request = new CreateLeadRequest("test@example.com", "John");
    request.setPhone("+123456789");
    request.setCompany("ACME");

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
    assertThat(response.getId()).isEqualTo(id);
    assertThat(response.getEmail()).isEqualTo("response@example.com");
    assertThat(response.getFirstName()).isEqualTo("Jane");
    assertThat(response.getPhone()).isEqualTo("+987654321");
    assertThat(response.getStatus()).isEqualTo("QUALIFIED");
    assertThat(response.getCreatedAt()).isEqualTo(now.atOffset(ZoneOffset.UTC));
    assertThat(response.getCompany()).isNull();
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

    UpdateLeadRequest request = new UpdateLeadRequest("newemail@example.com", "NewName");
    request.setPhone("+111");
    request.setCompany("UpdatedCompany");

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
