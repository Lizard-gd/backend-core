package ru.mentee.power.crm.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.repository.LeadRepository;

@Service
public class ParentService {

  private final LeadRepository leadRepository;
  private final ChildService childService;

  public ParentService(LeadRepository leadRepository, ChildService childService) {
    this.leadRepository = leadRepository;
    this.childService = childService;
  }

  @Transactional
  public void parentMethodWithRequired(UUID leadId, boolean throwInChild) {
    Lead lead = leadRepository.findById(leadId).orElseThrow();
    lead.setStatus("PARENT_UPDATED");
    leadRepository.save(lead);

    childService.requiredMethod(leadId, throwInChild);
  }

  @Transactional
  public void parentMethodWithRequiresNew(UUID leadId, boolean throwInChild) {
    Lead lead = leadRepository.findById(leadId).orElseThrow();
    lead.setStatus("PARENT_UPDATED");
    leadRepository.save(lead);

    childService.requiresNewMethod(leadId, throwInChild);
  }
}