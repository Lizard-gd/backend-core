package ru.mentee.power.crm.spring.rest;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import java.util.Optional;
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
        new Lead(UUID.randomUUID(), "John", "john@test.com", "+123", "NEW", LocalDateTime.now());
    Lead lead2 =
        new Lead(
            UUID.randomUUID(), "Jane", "jane@test.com", "+456", "QUALIFIED", LocalDateTime.now());
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
    Lead lead = new Lead(leadId, "John", "john@test.com", "+123", "NEW", LocalDateTime.now());
    when(leadService.findById(leadId)).thenReturn(Optional.of(lead));

    mockMvc
        .perform(get("/api/leads/{id}", leadId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(leadId.toString()))
        .andExpect(jsonPath("$.firstName").value("John"));
  }

  @Test
  void getLeadById_whenNotExists_shouldReturn404NotFound() throws Exception {
    UUID nonExistentId = UUID.randomUUID();
    when(leadService.findById(nonExistentId)).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/api/leads/{id}", nonExistentId))
        .andExpect(status().isNotFound())
        .andExpect(content().string(emptyOrNullString()));
  }

  @Test
  void createLead_shouldReturn201CreatedWithLocationHeader() throws Exception {
    Lead inputLead = new Lead();
    inputLead.setFirstName("Alice");
    inputLead.setEmail("alice@test.com");
    inputLead.setPhone("+789");
    inputLead.setStatus("NEW");

    UUID createdId = UUID.randomUUID();
    Lead createdLead =
        new Lead(createdId, "Alice", "alice@test.com", "+789", "NEW", LocalDateTime.now());
    when(leadService.addLead(any(), any(), any(), any(), any())).thenReturn(createdLead);

    mockMvc
        .perform(
            post("/api/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputLead)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/leads/" + createdId))
        .andExpect(jsonPath("$.id").value(createdId.toString()))
        .andExpect(jsonPath("$.firstName").value("Alice"));
  }

  @Test
  void updateLead_whenExists_shouldReturn200OkWithUpdatedLead() throws Exception {
    UUID leadId = UUID.randomUUID();
    Lead updatedLeadInput = new Lead();
    updatedLeadInput.setFirstName("Bob Updated");
    updatedLeadInput.setEmail("bob@test.com");
    updatedLeadInput.setPhone("+999");
    updatedLeadInput.setStatus("QUALIFIED");

    Lead updatedLead =
        new Lead(leadId, "Bob Updated", "bob@test.com", "+999", "QUALIFIED", LocalDateTime.now());
    when(leadService.updateLead(eq(leadId), any(Lead.class))).thenReturn(Optional.of(updatedLead));

    mockMvc
        .perform(
            put("/api/leads/{id}", leadId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedLeadInput)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Bob Updated"))
        .andExpect(jsonPath("$.status").value("QUALIFIED"));
  }

  @Test
  void updateLead_whenNotExists_shouldReturn404NotFound() throws Exception {
    UUID nonExistentId = UUID.randomUUID();
    Lead updatedLeadInput = new Lead();
    when(leadService.updateLead(eq(nonExistentId), any(Lead.class))).thenReturn(Optional.empty());

    mockMvc
        .perform(
            put("/api/leads/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedLeadInput)))
        .andExpect(status().isNotFound())
        .andExpect(content().string(emptyOrNullString()));
  }

  @Test
  void deleteLead_whenExists_shouldReturn204NoContent() throws Exception {
    UUID leadId = UUID.randomUUID();
    when(leadService.deleteLead(leadId)).thenReturn(true);

    mockMvc
        .perform(delete("/api/leads/{id}", leadId))
        .andExpect(status().isNoContent())
        .andExpect(content().string(emptyOrNullString()));
  }

  @Test
  void deleteLead_whenNotExists_shouldReturn404NotFound() throws Exception {
    UUID nonExistentId = UUID.randomUUID();
    when(leadService.deleteLead(nonExistentId)).thenReturn(false);

    mockMvc
        .perform(delete("/api/leads/{id}", nonExistentId))
        .andExpect(status().isNotFound())
        .andExpect(content().string(emptyOrNullString()));
  }
}
