package ru.mentee.power.crm.spring.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.service.LeadService;

@Controller
public class LeadController {

  private final LeadService leadService;

  public LeadController(LeadService leadService) {
    this.leadService = leadService;
  }

  @GetMapping("/leads")
  public String showLeads(
          @RequestParam(required = false) String status,
          Model model
  ) {
    List<Lead> leads;
    if (status == null) {
      leads = leadService.findAll();
    } else {
      leads = leadService.findByStatus(status);
    }

    model.addAttribute("leads", leads);
    model.addAttribute("currentFilter", status);
    return "leads/list";
  }
}
