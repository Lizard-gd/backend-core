package ru.mentee.power.crm.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.repository.LeadRepository;

@Service
public class LeadProcessor {

  private final LeadRepository leadRepository;

  public LeadProcessor(LeadRepository leadRepository) {
    this.leadRepository = leadRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void processSingleLead(UUID leadId) {
    Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));
    lead.setStatus("PROCESSED");
    leadRepository.save(lead);

    if (lead.getEmail().contains("2")) {
      throw new RuntimeException("Simulated error for lead: " + leadId);
    }
  }
}