package ru.mentee.power.crm.spring.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
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
    model.addAttribute("lead", new Lead(null,"", "", "", "", "NEW", LocalDateTime.now()));
    return "leads/create";
  }

  @PostMapping("/leads")
  public String createLead(@ModelAttribute Lead lead) {
    leadService.addLead(lead.firstName(), lead.email(), lead.phone(), lead.company(), lead.status());
    return "redirect:/leads";
  }

  @GetMapping("/leads")
  public String showLeads(
          @RequestParam(required = false) String search,
          @RequestParam(required = false) String status,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDateTime,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDateTime,
          Model model
  ) {
    List<Lead> leads = leadService.findLeads(search, status, fromDateTime, toDateTime);

    model.addAttribute("leads", leads);
    model.addAttribute("search", search != null ? search : "");
    model.addAttribute("status", status != null ? status: "");
    model.addAttribute("currentFilter", status);
    model.addAttribute("fromDateTime", fromDateTime);
    model.addAttribute("toDateTime", toDateTime);

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
    Lead updatedLead = new Lead(id, lead.firstName(), lead.email(), lead.phone(), lead.company(), lead.status(), lead.createdAt());
    leadService.update(id, updatedLead);
    return "redirect:/leads";
  }

  @PostMapping("/leads/{id}/delete")
  public String delete(@PathVariable String id) {
    leadService.delete(id);
    return "redirect:/leads";
  }
}
