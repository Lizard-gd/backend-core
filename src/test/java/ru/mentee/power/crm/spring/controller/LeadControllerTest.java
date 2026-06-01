package ru.mentee.power.crm.spring.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.repository.CompanyRepository;
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
class LeadControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private LeadService leadService;

  @MockitoBean private DealService dealService;

  @MockitoBean private CompanyRepository companyRepository;

  @MockitoBean private LeadRepository leadRepository;

  @MockitoBean private ChildService childService;

  @MockitoBean private LeadProcessor leadProcessor;

  @MockitoBean private ParentService parentService;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldShowCreateForm() throws Exception {
    when(companyRepository.findAll()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/leads/new"))
        .andExpect(status().isOk())
        .andExpect(view().name("leads/create"))
        .andExpect(model().attributeExists("lead"))
        .andExpect(model().attributeExists("companies"));
  }

  @Test
  void shouldCreateLead() throws Exception {
    when(leadService.addLead(any(), any(), any(), any(), any())).thenReturn(new Lead());
    when(companyRepository.findAll()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(
            post("/leads")
                .param("firstName", "John")
                .param("email", "john@example.com")
                .param("phone", "+123456789")
                .param("status", "NEW"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/leads"));
  }

  @Test
  void shouldReturnCreateFormWithError_whenFirstNameIsBlank() throws Exception {
    when(companyRepository.findAll()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(
            post("/leads")
                .param("firstName", "")
                .param("email", "john@example.com")
                .param("phone", "+123456789")
                .param("status", "NEW"))
        .andExpect(status().isOk())
        .andExpect(view().name("leads/create"))
        .andExpect(model().attributeHasFieldErrors("lead", "firstName"));
  }

  @Test
  void shouldReturnCreateFormWithError_whenEmailIsInvalid() throws Exception {
    when(companyRepository.findAll()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(
            post("/leads")
                .param("firstName", "John")
                .param("email", "invalid-email")
                .param("phone", "+123456789")
                .param("status", "NEW"))
        .andExpect(status().isOk())
        .andExpect(view().name("leads/create"))
        .andExpect(model().attributeHasFieldErrors("lead", "email"));
  }

  @Test
  void shouldReturnCreateFormWithError_whenEmailMissingDomain() throws Exception {
    when(companyRepository.findAll()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(
            post("/leads")
                .param("firstName", "John")
                .param("email", "john@example")
                .param("phone", "+123456789")
                .param("status", "NEW"))
        .andExpect(status().isOk())
        .andExpect(view().name("leads/create"))
        .andExpect(model().attributeHasFieldErrors("lead", "email"));
  }

  @Test
  void shouldReturnCreateFormWithError_whenPhoneHasNoPlus() throws Exception {
    when(companyRepository.findAll()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(
            post("/leads")
                .param("firstName", "John")
                .param("email", "john@example.com")
                .param("phone", "123456789")
                .param("status", "NEW"))
        .andExpect(status().isOk())
        .andExpect(view().name("leads/create"))
        .andExpect(model().attributeHasFieldErrors("lead", "phone"));
  }

  @Test
  void shouldShowAllLeadsWhenNoStatus() throws Exception {
    when(leadService.findLeads(null, null, null, null)).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/leads"))
        .andExpect(status().isOk())
        .andExpect(view().name("leads/list"))
        .andExpect(model().attributeExists("leads"));
  }

  @Test
  void shouldShowLeadsWithStatusFilter() throws Exception {
    when(leadService.findLeads(null, "NEW", null, null)).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/leads").param("status", "NEW"))
        .andExpect(status().isOk())
        .andExpect(view().name("leads/list"));
  }

  @Test
  void shouldCallFindLeadsWithOnlySearch_whenOnlySearchProvided() throws Exception {
    when(leadService.findLeads("john", null, null, null)).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/leads").param("search", "john")).andExpect(status().isOk());
    verify(leadService).findLeads("john", null, null, null);
  }

  @Test
  void shouldCallFindLeadsWithSearchAndStatus_whenBothParamsProvided() throws Exception {
    when(leadService.findLeads("john", "NEW", null, null)).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/leads").param("search", "john").param("status", "NEW"))
        .andExpect(status().isOk());
    verify(leadService).findLeads("john", "NEW", null, null);
  }

  @Test
  void shouldCallFindLeadsWithNullParams_whenNoParams() throws Exception {
    when(leadService.findLeads(null, null, null, null)).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/leads")).andExpect(status().isOk());
    verify(leadService).findLeads(null, null, null, null);
  }

  @Test
  void shouldReturnEditFormWithError_whenFirstNameBlankOnUpdate() throws Exception {
    UUID leadId = UUID.randomUUID();
    Lead existingLead = new Lead();
    existingLead.setId(leadId);
    existingLead.setFirstName("Old");
    existingLead.setEmail("old@example.com");
    existingLead.setPhone("+123456789");
    existingLead.setStatus("NEW");

    when(leadService.findById(leadId)).thenReturn(Optional.of(existingLead));
    when(companyRepository.findAll()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(
            post("/leads/" + leadId)
                .param("firstName", "")
                .param("email", "john@example.com")
                .param("phone", "+123456789")
                .param("status", "NEW"))
        .andExpect(status().isOk())
        .andExpect(view().name("spring/edit"))
        .andExpect(model().attributeHasFieldErrors("lead", "firstName"));
  }

  @Test
  void shouldRedirectAfterUpdate_whenAllFieldsValid() throws Exception {
    UUID leadId = UUID.randomUUID();
    Lead existingLead = new Lead();
    existingLead.setId(leadId);
    existingLead.setFirstName("Old");
    existingLead.setEmail("old@example.com");
    existingLead.setPhone("+123456789");
    existingLead.setStatus("NEW");

    when(leadService.findById(leadId)).thenReturn(Optional.of(existingLead));
    when(leadService.update(eq(leadId), any(Lead.class))).thenReturn(existingLead);
    when(companyRepository.findAll()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(
            post("/leads/" + leadId)
                .param("firstName", "Updated")
                .param("email", "updated@example.com")
                .param("phone", "+987654321")
                .param("status", "QUALIFIED"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/leads"));
  }

  @Test
  void shouldRedirectAfterCreate_whenAllFieldsValid() throws Exception {
    when(leadService.addLead(any(), any(), any(), any(), any())).thenReturn(new Lead());
    when(companyRepository.findAll()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(
            post("/leads")
                .param("firstName", "John")
                .param("email", "john@example.com")
                .param("phone", "+123456789")
                .param("status", "NEW"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/leads"));
  }
}
