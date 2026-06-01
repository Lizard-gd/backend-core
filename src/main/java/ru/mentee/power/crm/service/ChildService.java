package ru.mentee.power.crm.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.repository.LeadRepository;

@Service
public class ChildService {

  private final LeadRepository leadRepository;

  public ChildService(LeadRepository leadRepository) {
    this.leadRepository = leadRepository;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void requiredMethod(UUID leadId, boolean throwException) {
    Lead lead = leadRepository.findById(leadId).orElseThrow();
    lead.setStatus("CHILD_REQUIRED");
    leadRepository.save(lead);
    if (throwException) {
      throw new RuntimeException("Exception in REQUIRED method");
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void requiresNewMethod(UUID leadId, boolean throwException) {
    Lead lead = leadRepository.findById(leadId).orElseThrow();
    lead.setStatus("CHILD_REQUIRES_NEW");
    leadRepository.save(lead);
    if (throwException) {
      throw new RuntimeException("Exception in REQUIRES_NEW method");
    }
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void mandatoryMethod(UUID leadId) {
    Lead lead = leadRepository.findById(leadId).orElseThrow();
    lead.setStatus("CHILD_MANDATORY");
    leadRepository.save(lead);
  }
}
