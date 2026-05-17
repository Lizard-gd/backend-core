package ru.mentee.power.crm.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.repository.LeadRepository;

@Service
public class LeadLockingService {

  private final LeadRepository leadRepository;

  public LeadLockingService(LeadRepository leadRepository) {
    this.leadRepository = leadRepository;
  }

  @Transactional
  public Lead convertLeadToDealWithLock(UUID leadId, String newStatus) {
    Lead lead = leadRepository.findByIdForUpdate(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));
    lead.setStatus(newStatus);
    return leadRepository.save(lead);
  }

  @Transactional
  public Lead updateLeadStatusOptimistic(UUID leadId, String newStatus) {
    Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));
    lead.setStatus(newStatus);
    return leadRepository.save(lead);
  }
}