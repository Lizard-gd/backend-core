package ru.mentee.power.crm.spring.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.mentee.power.crm.model.Deal;
import ru.mentee.power.crm.model.DealStatus;
import ru.mentee.power.crm.service.DealService;
import ru.mentee.power.crm.service.LeadService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DealControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private DealService dealService;

  @MockitoBean private LeadService leadService;

  @Test
  void transitionStatus_validTransition_shouldRedirectToKanban() throws Exception {
    String dealId = "deal-123";
    Deal deal = new Deal(dealId, "lead-1", BigDecimal.TEN, DealStatus.QUALIFIED, null);
    when(dealService.transitionDealStatus(eq(dealId), eq(DealStatus.QUALIFIED))).thenReturn(deal);

    mockMvc
        .perform(post("/deals/{id}/transition", dealId).param("newStatus", "QUALIFIED"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/deals/kanban"));
  }

  @Test
  void transitionStatus_invalidTransition_shouldRedirectWithError() throws Exception {
    String dealId = "deal-456";
    when(dealService.transitionDealStatus(eq(dealId), eq(DealStatus.WON)))
        .thenThrow(new IllegalStateException("Cannot transition from PAUSE to WON"));

    mockMvc
        .perform(post("/deals/{id}/transition", dealId).param("newStatus", "WON"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/deals/kanban"))
        .andExpect(flash().attributeExists("error"));
  }

  @Test
  void pauseDeal_validPause_shouldRedirect() throws Exception {
    String dealId = "deal-789";
    Deal deal = new Deal(dealId, "lead-2", BigDecimal.valueOf(5000), DealStatus.PAUSE, null);
    when(dealService.transitionDealStatus(eq(dealId), eq(DealStatus.PAUSE))).thenReturn(deal);

    mockMvc
        .perform(post("/deals/{id}/pause", dealId))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/deals/kanban"));
  }

  @Test
  void pauseDeal_invalidPause_shouldRedirectWithError() throws Exception {
    String dealId = "deal-000";
    when(dealService.transitionDealStatus(eq(dealId), eq(DealStatus.PAUSE)))
        .thenThrow(new IllegalStateException("Cannot transition from WON to PAUSE"));

    mockMvc
        .perform(post("/deals/{id}/pause", dealId))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/deals/kanban"))
        .andExpect(flash().attributeExists("error"));
  }

  @Test
  void resumeDeal_validResume_shouldRedirect() throws Exception {
    String dealId = "deal-resume";
    Deal deal = new Deal(dealId, "lead-3", BigDecimal.valueOf(10000), DealStatus.QUALIFIED, null);
    when(dealService.transitionDealStatus(eq(dealId), eq(DealStatus.QUALIFIED))).thenReturn(deal);

    mockMvc
        .perform(post("/deals/{id}/resume", dealId).param("targetStatus", "QUALIFIED"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/deals/kanban"));
  }

  @Test
  void resumeDeal_invalidResume_shouldRedirectWithError() throws Exception {
    String dealId = "deal-resume-invalid";
    when(dealService.transitionDealStatus(eq(dealId), eq(DealStatus.WON)))
        .thenThrow(new IllegalStateException("Cannot transition from PAUSE to WON"));

    mockMvc
        .perform(post("/deals/{id}/resume", dealId).param("targetStatus", "WON"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/deals/kanban"))
        .andExpect(flash().attributeExists("error"));
  }
}
