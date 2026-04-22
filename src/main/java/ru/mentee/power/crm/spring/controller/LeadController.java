package ru.mentee.power.crm.spring.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.service.LeadService;

@Controller
public class LeadController {

  private final LeadService leadService;

  public LeadController(LeadService leadService) {
    this.leadService = leadService;
  }

  @GetMapping("/leads/new")
  public String showCreateForm(Model model) {
    model.addAttribute("lead", new Lead(null, "", "", "", "NEW"));
    return "leads/create";
  }

  @PostMapping("/leads")
  public String createLead(@ModelAttribute Lead lead) {
    leadService.addLead(lead.email(), lead.phone(), lead.company(), lead.status());
    return "redirect:/leads";
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

  @GetMapping("/")
  @ResponseBody
  public String home() {
    return "Spring Boot CRM is running! Beans created: " + leadService.findAll().size() + " leads.";
  }

  @GetMapping("/leads/{id}/edit")
  public String showEditForm(@PathVariable String id, Model model) {
    Optional<Lead> leadOpt = leadService.findById(id);
    if (leadOpt.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead not found with id: " + id);
    }
    model.addAttribute("lead", leadOpt.get());
    return "spring/edit";
  }

  @PostMapping("/leads/{id}")
  public String updateLead(@PathVariable String id, @ModelAttribute Lead lead) {
    Lead updatedLead = new Lead(id, lead.email(), lead.phone(), lead.company(), lead.status());
    leadService.update(id, updatedLead);
    return "redirect:/leads";
  }
}
