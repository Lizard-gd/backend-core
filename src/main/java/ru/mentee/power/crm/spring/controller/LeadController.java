package ru.mentee.power.crm.spring.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
    Lead emptyLead = new Lead();
    emptyLead.setStatus("NEW");
    emptyLead.setCreatedAt(LocalDateTime.now());
    model.addAttribute("lead", emptyLead);
    return "leads/create";
  }

  @PostMapping("/leads")
  public String createLead(@Valid @ModelAttribute("lead") Lead lead,
                           BindingResult result, Model model) {
    if (result.hasErrors()) {
      model.addAttribute("lead", lead);
      model.addAttribute("errors", result);
      return "leads/create";
    }
    try {
      leadService.addLead(lead.getFirstName(), lead.getEmail(),
              lead.getPhone(), lead.getCompany(), lead.getStatus());
    } catch (IllegalStateException e) {
      result.rejectValue("email", "error.duplicate", "Лид с таким email уже существует");
      model.addAttribute("lead", lead);
      model.addAttribute("errors", result);
      return "leads/create";
    }
    return "redirect:/leads";
  }

  @GetMapping("/leads")
  public String showLeads(
          @RequestParam(required = false) String search,
          @RequestParam(required = false) String status,
          @RequestParam(required = false) @DateTimeFormat(iso
                  = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDateTime,
          @RequestParam(required = false) @DateTimeFormat(iso
                  = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDateTime,
          Model model
  ) {
    List<Lead> leads = leadService.findLeads(search, status, fromDateTime, toDateTime);

    model.addAttribute("leads", leads);
    model.addAttribute("search", search != null ? search : "");
    model.addAttribute("status", status != null ? status : "");
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
    UUID uuid = UUID.fromString(id);
    Optional<Lead> leadOpt = leadService.findById(uuid);
    if (leadOpt.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead not found with id: " + id);
    }
    model.addAttribute("lead", leadOpt.get());
    return "spring/edit";
  }

  @PostMapping("/leads/{id}")
  public String updateLead(@PathVariable String id, @Valid @ModelAttribute("lead") Lead lead,
                           BindingResult result, Model model) {
    if (result.hasErrors()) {
      model.addAttribute("lead", lead);
      model.addAttribute("errors", result);
      return "spring/edit";
    }
    UUID uuid = UUID.fromString(id);
    leadService.update(uuid, lead);
    return "redirect:/leads";
  }

  @PostMapping("/leads/{id}/delete")
  public String delete(@PathVariable String id) {
    UUID uuid = UUID.fromString(id);
    leadService.delete(uuid);
    return "redirect:/leads";
  }
}
