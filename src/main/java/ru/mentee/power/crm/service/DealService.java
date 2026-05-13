package ru.mentee.power.crm.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import ru.mentee.power.crm.model.Deal;
import ru.mentee.power.crm.model.DealStatus;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.repository.DealRepository;
import ru.mentee.power.crm.repository.LeadRepository;

@Service
public class DealService {
  private final DealRepository dealRepository;
  private final LeadRepository leadRepository;

  public DealService(DealRepository dealRepository, LeadRepository leadRepository) {
    this.dealRepository = dealRepository;
    this.leadRepository = leadRepository;
  }

  public Deal convertLeadToDeal(String leadId, BigDecimal amount) {
    UUID leadUuid = UUID.fromString(leadId);
    Lead lead = leadRepository.findById(leadUuid)
            .orElseThrow(() -> new IllegalArgumentException("Lead not found with id: " +  leadId));

    if (!"QUALIFIED".equals(lead.getStatus())) {
      throw new IllegalStateException("Only QUALIFIED leads can be converted to deals. "
                + "Current status: " + lead.getStatus());
    }

    Deal newDeal = new Deal(leadId, amount);
    dealRepository.save(newDeal);
    return newDeal;
  }

  public Deal transitionDealStatus(String dealId, DealStatus newStatus) {
    Deal deal = dealRepository.findById(dealId)
            .orElseThrow(() -> new IllegalArgumentException("Deal not found with id: " + dealId));
    deal.transitionTo(newStatus);
    dealRepository.save(deal);
    return deal;
  }

  public List<Deal> getAllDeals() {
    return dealRepository.findAll();
  }

  public Map<DealStatus, List<Deal>> getDealsByStatusForKanban() {
    return dealRepository.findAll().stream()
            .collect(Collectors.groupingBy(Deal::getStatus));
  }

  public java.util.Optional<Deal> findById(String id) {
    return dealRepository.findById(id);
  }
}