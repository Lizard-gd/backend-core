package ru.mentee.power.crm.spring.controller;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.mentee.power.crm.model.DealStatus;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.service.DealService;
import ru.mentee.power.crm.service.LeadService;

@Controller
@RequestMapping("/deals")
public class DealController {

  private final DealService dealService;
  private final LeadService leadService;

  public DealController(DealService dealService, LeadService leadService) {
    this.dealService = dealService;
    this.leadService = leadService;
  }

  @GetMapping
  public String listDeals(Model model) {
    model.addAttribute("deals", dealService.getAllDeals());
    return "deals/list";
  }

  @GetMapping("/kanban")
  public String kanbanView(Model model) {
    model.addAttribute("dealsByStatus", dealService.getDealsByStatusForKanban());
    return "deals/kanban";
  }

  @GetMapping("/convert/{leadId}")
  public String showConvertForm(@PathVariable String leadId, Model model) {
    UUID uuid = UUID.fromString(leadId);
    Lead lead = leadService.findById(uuid)
              .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));
    if (!"QUALIFIED".equals(lead.getStatus())) {
      throw new IllegalStateException("Only QUALIFIED leads "
              + "can be converted to deals. Lead status: "
                  + lead.getStatus());
    }
    model.addAttribute("lead", lead);
    return "deals/convert";
  }

  @PostMapping("/convert")
  public String convertLeadToDeal(@RequestParam String leadId, @RequestParam BigDecimal amount) {
    dealService.convertLeadToDeal(leadId, amount);
    return "redirect:/deals";
  }

  @PostMapping("/{id}/transition")
  public String transitionStatus(@PathVariable String id,
                                 @RequestParam DealStatus newStatus,
                                 RedirectAttributes redirectAttributes) {
    try {
      dealService.transitionDealStatus(id, newStatus);
    } catch (IllegalStateException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/deals/kanban";
    }
    return "redirect:/deals/kanban";
  }
}
