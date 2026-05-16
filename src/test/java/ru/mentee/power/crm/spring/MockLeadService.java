package ru.mentee.power.crm.spring;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.service.LeadService;

public class MockLeadService extends LeadService {

  private final List<Lead> mockLeads;

  public MockLeadService() {
    super(null, null, null);
    Lead lead1 = new Lead();
    lead1.setId(UUID.randomUUID());
    lead1.setFirstName("Test Lead 1");
    lead1.setEmail("test1@example.com");
    lead1.setPhone("+1234567890");
    lead1.setCompany("Test Corp1");
    lead1.setStatus("NEW");
    lead1.setCreatedAt(LocalDateTime.now());

    Lead lead2 = new Lead();
    lead2.setId(UUID.randomUUID());
    lead2.setFirstName("Test Lead 2");
    lead2.setEmail("test2@example.com");
    lead2.setPhone("+0987654321");
    lead2.setCompany("Test Corp2");
    lead2.setStatus("NEW");
    lead2.setCreatedAt(LocalDateTime.now());

    this.mockLeads = List.of(lead1, lead2);
  }

  @Override
  public List<Lead> findAll() {
    return mockLeads;
  }

  @Override
  public List<Lead> findByStatus(String status) {
    return mockLeads.stream()
            .filter(lead -> lead.getStatus().equals(status))
            .toList();
  }
}