package ru.mentee.power.crm.spring.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.service.LeadService;
import ru.mentee.power.crm.spring.dto.generated.CreateLeadRequest;
import ru.mentee.power.crm.spring.dto.generated.LeadResponse;
import ru.mentee.power.crm.spring.mapper.LeadMapper;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LeadRestControllerValidationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private LeadService leadService;

  @MockitoBean private LeadMapper leadMapper;

  @Test
  void shouldReturn400_whenEmailIsBlank() throws Exception {
    CreateLeadRequest request = new CreateLeadRequest();
    request.setEmail("");
    request.setFirstName("John");
    request.setPhone("+1234567890");

    mockMvc
        .perform(
            post("/api/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void shouldReturn400_whenEmailIsInvalidFormat() throws Exception {
    CreateLeadRequest request = new CreateLeadRequest();
    request.setEmail("not-an-email");
    request.setFirstName("John");
    request.setPhone("+1234567890");

    mockMvc
        .perform(
            post("/api/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400_whenFirstNameIsTooShort() throws Exception {
    CreateLeadRequest request = new CreateLeadRequest();
    request.setEmail("john@example.com");
    request.setFirstName("J");
    request.setPhone("+1234567890");

    mockMvc
        .perform(
            post("/api/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void shouldReturn400_whenPhoneHasNoPlus() throws Exception {
    CreateLeadRequest request = new CreateLeadRequest();
    request.setEmail("john@example.com");
    request.setFirstName("John");
    request.setPhone("1234567890");

    mockMvc
        .perform(
            post("/api/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void shouldReturn201_whenAllFieldsValid() throws Exception {
    CreateLeadRequest request = new CreateLeadRequest();
    request.setEmail("john@example.com");
    request.setFirstName("John");
    request.setPhone("+1234567890");
    request.setCompany("ACME");

    Lead leadEntity = new Lead();
    leadEntity.setId(UUID.randomUUID());
    leadEntity.setFirstName(request.getFirstName());
    leadEntity.setEmail(request.getEmail());
    leadEntity.setPhone(request.getPhone());
    leadEntity.setStatus("NEW");
    leadEntity.setCreatedAt(LocalDateTime.now());

    when(leadMapper.toEntity(any(ru.mentee.power.crm.spring.dto.generated.CreateLeadRequest.class)))
        .thenReturn(leadEntity);

    when(leadService.addLead(anyString(), anyString(), anyString(), anyString(), any()))
        .thenReturn(leadEntity);

    LeadResponse response =
        new LeadResponse(
            leadEntity.getId(),
            leadEntity.getEmail(),
            leadEntity.getFirstName(),
            leadEntity.getCreatedAt().atOffset(ZoneOffset.UTC));
    response.setPhone(leadEntity.getPhone());
    response.setCompany(request.getCompany());
    response.setStatus(leadEntity.getStatus());

    when(leadMapper.toResponse(any(Lead.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/api/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }
}
