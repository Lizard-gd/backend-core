package ru.mentee.power.crm.spring.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
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
import ru.mentee.power.crm.repository.CompanyRepository;
import ru.mentee.power.crm.repository.DealRepository;
import ru.mentee.power.crm.repository.LeadRepository;
import ru.mentee.power.crm.service.ChildService;
import ru.mentee.power.crm.service.DealService;
import ru.mentee.power.crm.service.LeadProcessor;
import ru.mentee.power.crm.service.LeadService;
import ru.mentee.power.crm.service.ParentService;
import ru.mentee.power.crm.spring.dto.generated.CreateLeadRequest;
import ru.mentee.power.crm.spring.dto.generated.UpdateLeadRequest;
import ru.mentee.power.crm.spring.exception.EntityNotFoundException;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LeadRestControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private LeadService leadService;

  @MockitoBean private DealService dealService;

  @MockitoBean private LeadRepository leadRepository;

  @MockitoBean private ChildService childService;

  @MockitoBean private ParentService parentService;

  @MockitoBean private LeadProcessor leadProcessor;

  @MockitoBean private CompanyRepository companyRepository;

  @MockitoBean private DealRepository dealRepository;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void getAllLeads_shouldReturn200OkWithList() throws Exception {
    Lead lead1 =
        new Lead(UUID.randomUUID(), "John", "john@test.com", "+124233", "NEW", LocalDateTime.now());
    Lead lead2 =
        new Lead(
            UUID.randomUUID(),
            "Jane",
            "jane@test.com",
            "+456456",
            "QUALIFIED",
            LocalDateTime.now());
    List<Lead> leads = List.of(lead1, lead2);
    when(leadService.findAll()).thenReturn(leads);

    mockMvc
        .perform(get("/api/leads"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].firstName").value("John"))
        .andExpect(jsonPath("$[1].email").value("jane@test.com"));
  }

  @Test
  void getLeadById_whenExists_shouldReturn200OkWithLead() throws Exception {
    UUID leadId = UUID.randomUUID();
    Lead lead = new Lead(leadId, "John", "john@test.com", "+121233", "NEW", LocalDateTime.now());
    when(leadService.getLeadById(leadId)).thenReturn(lead);

    mockMvc
        .perform(get("/api/leads/{id}", leadId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(leadId.toString()))
        .andExpect(jsonPath("$.firstName").value("John"));
  }

  @Test
  void getLeadById_whenNotExists_shouldReturn404NotFound() throws Exception {
    UUID nonExistentId = UUID.randomUUID();
    when(leadService.getLeadById(nonExistentId))
        .thenThrow(new EntityNotFoundException("Lead", nonExistentId.toString()));

    mockMvc
        .perform(get("/api/leads/{id}", nonExistentId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Lead not found with id: " + nonExistentId));
  }

  @Test
  void createLead_shouldReturn201CreatedWithLocationHeader() throws Exception {
    CreateLeadRequest request = new CreateLeadRequest("alice@test.com", "Alice");
    request.setPhone("+787659");
    request.setCompany(null);
    UUID createdId = UUID.randomUUID();
    Lead createdLead =
        new Lead(createdId, "Alice", "alice@test.com", "+789546", "NEW", LocalDateTime.now());
    when(leadService.addLead(any(), any(), any(), any(), any())).thenReturn(createdLead);

    mockMvc
        .perform(
            post("/api/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/leads/" + createdId))
        .andExpect(jsonPath("$.id").value(createdId.toString()))
        .andExpect(jsonPath("$.firstName").value("Alice"));
  }

  @Test
  void updateLead_whenExists_shouldReturn200OkWithUpdatedLead() throws Exception {
    UUID leadId = UUID.randomUUID();
    Lead existingLead =
        new Lead(leadId, "Bob", "bob@old.com", "+000000", "NEW", LocalDateTime.now());
    UpdateLeadRequest updateRequest = new UpdateLeadRequest("bob@test.com", "Bob Updated");
    updateRequest.setPhone("+999777");
    updateRequest.setCompany("ACME");
    Lead updatedLead =
        new Lead(
            leadId, "Bob Updated", "bob@test.com", "+999888", "QUALIFIED", LocalDateTime.now());

    when(leadService.getLeadById(leadId)).thenReturn(existingLead);
    when(leadService.updateLeadOrThrow(leadId, existingLead)).thenReturn(updatedLead);

    mockMvc
        .perform(
            put("/api/leads/{id}", leadId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Bob Updated"))
        .andExpect(jsonPath("$.status").value("QUALIFIED"));
  }

  @Test
  void updateLead_whenNotExists_shouldReturn404NotFound() throws Exception {
    UUID nonExistentId = UUID.randomUUID();
    UpdateLeadRequest updateRequest = new UpdateLeadRequest("any@test.com", "Any");
    updateRequest.setPhone("+1234567890");
    updateRequest.setCompany("AnyCo");

    when(leadService.getLeadById(nonExistentId))
        .thenThrow(new EntityNotFoundException("Lead", nonExistentId.toString()));

    mockMvc
        .perform(
            put("/api/leads/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Lead not found with id: " + nonExistentId));
  }

  @Test
  void deleteLead_whenExists_shouldReturn204NoContent() throws Exception {
    UUID leadId = UUID.randomUUID();
    Lead lead = new Lead(leadId, "John", "john@test.com", "+123", "NEW", LocalDateTime.now());
    when(leadService.getLeadById(leadId)).thenReturn(lead);
    doNothing().when(leadService).deleteLeadOrThrow(leadId);

    mockMvc.perform(delete("/api/leads/{id}", leadId)).andExpect(status().isNoContent());
  }

  @Test
  void deleteLead_whenNotExists_shouldReturn404NotFound() throws Exception {
    UUID nonExistentId = UUID.randomUUID();
    doThrow(new EntityNotFoundException("Lead", nonExistentId.toString()))
        .when(leadService)
        .deleteLeadOrThrow(nonExistentId);

    mockMvc
        .perform(delete("/api/leads/{id}", nonExistentId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Lead not found with id: " + nonExistentId));
  }
}
