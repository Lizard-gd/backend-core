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

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.service.LeadService;

@WebMvcTest(LeadController.class)
@AutoConfigureMockMvc
class LeadControllerEditTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private LeadService leadService;

  @Test
  void shouldShowEditFormWithPrefilledData() throws Exception {
    Lead testLead = new Lead("123", "test@example.com", "+123456789", "Test Corp", "NEW");
    when(leadService.findById("123")).thenReturn(Optional.of(testLead));

    mockMvc.perform(get("/leads/123/edit"))
              .andExpect(status().isOk())
              .andExpect(view().name("spring/edit"))
              .andExpect(model().attributeExists("lead"))
              .andExpect(model().attribute("lead", testLead));
  }

  @Test
  void shouldUpdateLeadAndRedirect() throws Exception {
    mockMvc.perform(post("/leads/123")
                      .param("email", "updated@example.com")
                      .param("phone", "+222")
                      .param("company", "Updated Corp")
                      .param("status", "QUALIFIED")
                      .param("id", "123"))
              .andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/leads"));

    verify(leadService).update(eq("123"), any(Lead.class));
  }

  @Test
  void shouldReturn404WhenLeadNotFound() throws Exception {
    when(leadService.findById("non-existent")).thenReturn(Optional.empty());

    mockMvc.perform(get("/leads/non-existent/edit"))
              .andExpect(status().isNotFound());
  }
}