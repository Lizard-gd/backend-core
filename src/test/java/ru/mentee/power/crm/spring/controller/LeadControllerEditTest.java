package ru.mentee.power.crm.spring.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.service.DealService;
import ru.mentee.power.crm.service.LeadService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
  "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
})
class LeadControllerEditTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private LeadService leadService;

  @MockitoBean
  private DealService dealService;

  @Test
  void shouldShowEditFormWithPrefilledData() throws Exception {
    UUID leadId = UUID.randomUUID();
    Lead testLead = new Lead();
    testLead.setId(leadId);
    testLead.setFirstName("John");
    testLead.setEmail("test@example.com");
    testLead.setPhone("+123456789");
    testLead.setCompany("Test Corp");
    testLead.setStatus("NEW");
    testLead.setCreatedAt(LocalDateTime.now());

    when(leadService.findById(leadId)).thenReturn(Optional.of(testLead));

    mockMvc.perform(get("/leads/{id}/edit", leadId.toString()))
            .andExpect(status().isOk())
            .andExpect(view().name("spring/edit"))
            .andExpect(model().attributeExists("lead"));
  }

  @Test
  void shouldUpdateLeadAndRedirect() throws Exception {
    UUID leadId = UUID.randomUUID();
    mockMvc.perform(post("/leads/{id}", leadId.toString())
                    .param("firstName", "UpdatedName")
                    .param("email", "updated@example.com")
                    .param("phone", "+222222")
                    .param("company", "Updated Corp")
                    .param("status", "QUALIFIED"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/leads"));

    verify(leadService).update(eq(leadId), any(Lead.class));
  }

  @Test
  void shouldReturn404WhenLeadNotFound() throws Exception {
    UUID nonExistent = UUID.randomUUID();
    when(leadService.findById(nonExistent)).thenReturn(Optional.empty());

    mockMvc.perform(get("/leads/{id}/edit", nonExistent.toString()))
            .andExpect(status().isNotFound());
  }

  @Test
  void shouldDeleteLeadAndRedirect() throws Exception {
    UUID leadId = UUID.randomUUID();
    doNothing().when(leadService).delete(leadId);

    mockMvc.perform(post("/leads/{id}/delete", leadId.toString()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/leads"));

    verify(leadService).delete(leadId);
  }

  @Test
  void shouldReturn404WhenLeadNotFound_forDelete() throws Exception {
    UUID nonExistent = UUID.randomUUID();
    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead not found"))
            .when(leadService).delete(nonExistent);

    mockMvc.perform(post("/leads/{id}/delete", nonExistent.toString()))
            .andExpect(status().isNotFound());
  }
}