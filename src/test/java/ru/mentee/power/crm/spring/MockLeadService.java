package ru.mentee.power.crm.spring;

import java.time.LocalDateTime;
import java.util.List;

import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.service.LeadService;

public class MockLeadService extends LeadService {
  private final List<Lead> mockLeads;

  public MockLeadService() {
    super(null);
    this.mockLeads = List.of(
            new Lead("Test Lead 1", "test", "test1@example.com",
                    "+1234567890", "Test Corp1", "NEW",  LocalDateTime.now()),
            new Lead("Test Lead 2", "test", "test2@example.com",
                    "+0987654321", "Test Corp2", "NEW",  LocalDateTime.now())
    );
  }

  @Override
  public List<Lead> findAll() {
    return mockLeads;
  }

  @Override
  public List<Lead> findByStatus(String status) {
    return mockLeads.stream()
            .filter(lead -> lead.status().equals(status))
            .toList();
  }
}
