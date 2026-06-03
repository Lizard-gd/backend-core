package ru.mentee.power.crm.service;

import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.mentee.power.crm.dto.CreateDealRequest;
import ru.mentee.power.crm.model.Company;
import ru.mentee.power.crm.model.Deal;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.repository.CompanyRepository;
import ru.mentee.power.crm.repository.DealRepository;
import ru.mentee.power.crm.repository.LeadRepository;
import ru.mentee.power.crm.spring.client.EmailValidationFeignClient;
import ru.mentee.power.crm.spring.client.EmailValidationResponse;

@Service
public class LeadService {

  private static final Logger log = LoggerFactory.getLogger(LeadService.class);
  private final LeadRepository repository;
  private final DealRepository dealRepository;
  private final LeadProcessor leadProcessor;
  private final CompanyRepository companyRepository;
  private final EmailValidationFeignClient emailValidationClient;

  public LeadService(
      LeadRepository repository,
      DealRepository dealRepository,
      LeadProcessor leadProcessor,
      CompanyRepository companyRepository,
      EmailValidationFeignClient emailValidationClient) {
    this.repository = repository;
    this.dealRepository = dealRepository;
    this.leadProcessor = leadProcessor;
    this.companyRepository = companyRepository;
    this.emailValidationClient = emailValidationClient;
    log.info("LeadService constructor called");
  }

  @PostConstruct
  void init() {
    log.info("LeadService @PostConstruct init() called - Bean lifecycle phase");
  }

  @Retry(name = "email-validation", fallbackMethod = "addLeadFallback")
  public Lead addLead(String firstName, String email, String phone, String status, UUID companyId) {
    try {
      EmailValidationResponse validation = emailValidationClient.validateEmail(email);
      if (!validation.valid()) {
        throw new IllegalArgumentException("Email not valid: " + validation.reason());
      }
    } catch (feign.FeignException.BadRequest | feign.FeignException.NotFound e) {
      throw e;
    } catch (Exception e) {
      log.warn("Email validation failed: {}", e.getMessage());
      throw new RuntimeException("Email validation service unavailable", e);
    }

    Optional<Lead> existing = repository.findByEmailNative(email);
    if (existing.isPresent()) {
      throw new IllegalStateException("Lead with email already exists: " + email);
    }

    Lead newLead = new Lead();
    newLead.setFirstName(firstName);
    newLead.setEmail(email);
    newLead.setPhone(phone);
    newLead.setStatus(status);
    newLead.setCreatedAt(LocalDateTime.now());

    if (companyId != null) {
      Company company =
          companyRepository
              .findById(companyId)
              .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
      newLead.setCompany(company);
    }

    return repository.save(newLead);
  }

  public Lead addLeadFallback(
      String firstName, String email, String phone, String status, UUID companyId, Throwable t) {
    log.warn(
        "Fallback: email validation failed, creating lead without validation. Error: {}",
        t.getMessage());

    Lead newLead = new Lead();
    newLead.setFirstName(firstName);
    newLead.setEmail(email);
    newLead.setPhone(phone);
    newLead.setStatus(status);
    newLead.setCreatedAt(LocalDateTime.now());

    if (companyId != null) {
      Company company =
          companyRepository
              .findById(companyId)
              .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
      newLead.setCompany(company);
    }

    return repository.save(newLead);
  }

  public Lead addLead(String firstName, String email, String phone, String status) {
    return addLead(firstName, email, phone, status, null);
  }

  public List<Lead> findAll() {
    return repository.findAll();
  }

  public List<Lead> findByStatus(String status) {
    return repository.findByStatusNative(status);
  }

  public Optional<Lead> findById(UUID id) {
    return repository.findById(id);
  }

  public Optional<Lead> findByEmail(String email) {
    return repository.findByEmailNative(email);
  }

  @Transactional
  public Lead update(UUID id, Lead updatedLead) {
    Lead existing =
        repository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Lead not found with id: " + id));

    existing.setFirstName(updatedLead.getFirstName());
    existing.setEmail(updatedLead.getEmail());
    existing.setPhone(updatedLead.getPhone());
    existing.setStatus(updatedLead.getStatus());

    if (updatedLead.getCompany() != null) {
      existing.setCompany(updatedLead.getCompany());
    }

    return repository.save(existing);
  }

  public void delete(UUID id) {
    Optional<Lead> existing = repository.findById(id);
    if (existing.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead not found with id: " + id);
    }
    repository.deleteById(id);
  }

  public Optional<Lead> updateLead(UUID id, Lead updatedLead) {
    Optional<Lead> existingOpt = repository.findById(id);

    if (existingOpt.isEmpty()) {
      return Optional.empty();
    }

    Lead existing = existingOpt.get();
    existing.setFirstName(updatedLead.getFirstName());
    existing.setEmail(updatedLead.getEmail());
    existing.setPhone(updatedLead.getPhone());
    existing.setStatus(updatedLead.getStatus());

    Lead saved = repository.save(existing);

    return Optional.of(saved);
  }

  public boolean deleteLead(UUID id) {
    if (repository.existsById(id)) {
      repository.deleteById(id);
      return true;
    } else {
      return false;
    }
  }

  public List<Lead> findLeads(
      String search, String status, LocalDateTime fromDateTime, LocalDateTime toDateTime) {
    List<Lead> allLeads = repository.findAll();

    var stream = allLeads.stream();

    if (search != null && !search.isBlank()) {
      String lowerSearch = search.toLowerCase();
      stream =
          stream.filter(
              lead ->
                  lead.getFirstName().toLowerCase().contains(lowerSearch)
                      || lead.getEmail().toLowerCase().contains(lowerSearch));
    }
    if (status != null && !status.isBlank()) {
      stream = stream.filter(lead -> lead.getStatus().equals(status));
    }
    if (fromDateTime != null) {
      stream = stream.filter(lead -> !lead.getCreatedAt().isBefore(fromDateTime));
    }
    if (toDateTime != null) {
      stream = stream.filter(lead -> !lead.getCreatedAt().isAfter(toDateTime));
    }
    return stream.collect(Collectors.toList());
  }

  public Optional<Lead> findByEmailDerived(String email) {
    return repository.findByEmail(email);
  }

  public List<Lead> findByStatuses(List<String> statuses) {
    return repository.findByStatusIn(statuses);
  }

  public Page<Lead> getLeadsByCompany(String companyName, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    return repository.findByCompanyName(companyName, pageable);
  }

  @Transactional
  public int bulkUpdateStatus(String oldStatus, String newStatus) {
    int updated = repository.updateStatusBulk(oldStatus, newStatus);
    System.out.println(
        "Bulk update: " + updated + " leads changed from " + oldStatus + " to " + newStatus);
    return updated;
  }

  @Transactional
  public int bulkDeleteByStatus(String status) {
    int deleted = repository.deleteByStatusBulk(status);
    System.out.println("Bulk delete: " + deleted + " leads with status " + status + " removed");
    return deleted;
  }

  @Transactional
  public Deal convertLeadToDeal(UUID leadId, CreateDealRequest request) {
    Lead lead =
        repository
            .findById(leadId)
            .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));

    if (!"QUALIFIED".equals(lead.getStatus())) {
      throw new IllegalStateException(
          "Lead " + leadId + " cannot be converted. Current status: " + lead.getStatus());
    }

    Deal newDeal = new Deal(leadId.toString(), request.getAmount());
    dealRepository.save(newDeal);

    lead.setStatus("CONVERTED");
    repository.save(lead);

    return newDeal;
  }

  @Transactional
  public void processLeads(List<UUID> leadIds) {
    for (UUID id : leadIds) {
      leadProcessor.processSingleLead(id);
    }
  }
}
