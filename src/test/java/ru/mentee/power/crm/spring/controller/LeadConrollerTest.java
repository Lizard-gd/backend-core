package ru.mentee.power.crm.spring.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.mentee.power.crm.service.LeadService;

@WebMvcTest(LeadController.class)
public class LeadConrollerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private LeadService leadService;

  @Test
  void shouldShowCreateForm() throws Exception {
    mockMvc.perform(get("/leads/new"))
              .andExpect(status().isOk())
              .andExpect(view().name("leads/create"))
              .andExpect(model().attributeExists("lead"));
  }

  @Test
  void shouldCreateLead() throws Exception {
    mockMvc.perform(post("/leads")
                      .param("email", "new@example.com")
                      .param("phone", "+123456789")
                      .param("company", "New Corp")
                      .param("status", "NEW"))
              .andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/leads"));

    verify(leadService).addLead("new@example.com", "+123456789", "New Corp", "NEW");
  }

  @Test
  void shouldShowLeadsWithStatusFilter() throws Exception {
    when(leadService.findByStatus("QUALIFIED")).thenReturn(List.of());

    mockMvc.perform(get("/leads").param("status", "QUALIFIED"))
              .andExpect(status().isOk())
              .andExpect(view().name("leads/list"))
              .andExpect(model().attributeExists("leads"))
              .andExpect(model().attribute("currentFilter", "QUALIFIED"));

    verify(leadService).findByStatus("QUALIFIED");
  }

  @Test
  void shouldShowAllLeadsWhenNoStatus() throws Exception {
    when(leadService.findAll()).thenReturn(List.of());

    mockMvc.perform(get("/leads"))
              .andExpect(status().isOk())
              .andExpect(view().name("leads/list"))
              .andExpect(model().attribute("currentFilter", nullValue()));

    verify(leadService).findAll();
  }
}