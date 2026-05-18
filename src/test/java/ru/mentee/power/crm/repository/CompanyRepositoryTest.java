package ru.mentee.power.crm.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.power.crm.model.Company;
import ru.mentee.power.crm.model.Lead;

@SpringBootTest
@Transactional
class CompanyRepositoryTest {

  @Autowired
  private CompanyRepository companyRepository;

  @Autowired
  private LeadRepository leadRepository;

  @Autowired
  private jakarta.persistence.EntityManager entityManager;

  @BeforeEach
  void cleanUp() {
    leadRepository.deleteAll();
    companyRepository.deleteAll();
  }

  @Test
  void shouldSaveCompanyWithLeads_whenCascadePersist() {
    Company company = new Company();
    company.setName("Яндекс");
    company.setIndustry("IT");

    Lead lead1 = new Lead("Алексей", "alex@yandex.ru",
            "+71234567890", "NEW", LocalDateTime.now());
    Lead lead2 = new Lead("Мария", "maria@yandex.ru",
            "+79876543210", "CONTACTED", LocalDateTime.now());
    Lead lead3 = new Lead("Дмитрий", "dmitry@yandex.ru",
            "+75553332211", "QUALIFIED", LocalDateTime.now());

    company.addLead(lead1);
    company.addLead(lead2);
    company.addLead(lead3);

    Company savedCompany = companyRepository.save(company);

    assertThat(savedCompany.getId()).isNotNull();
    assertThat(savedCompany.getLeads()).hasSize(3);

    Company foundCompany = companyRepository.findById(savedCompany.getId()).orElseThrow();
    assertThat(foundCompany.getLeads())
        .hasSize(3)
        .extracting(Lead::getEmail)
        .containsExactlyInAnyOrder("alex@yandex.ru", "maria@yandex.ru", "dmitry@yandex.ru");

    for (Lead lead : foundCompany.getLeads()) {
      assertThat(lead.getCompany()).isEqualTo(foundCompany);
    }
  }

  @Test
  void shouldLoadCompanyWithLeadsUsingEntityGraph_singleQuery() {
    Company company = new Company();
    company.setName("Тинькофф");
    company.setIndustry("Finance");

    for (int i = 1; i <= 3; i++) {
      Lead lead = new Lead(
              "Клиент" + i,
              "client" + i + "@tinkoff.ru",
              "+7000000000" + i,
              "NEW",
              LocalDateTime.now()
      );
      company.addLead(lead);
    }

    Company savedCompany = companyRepository.save(company);
    UUID companyId = savedCompany.getId();

    entityManager.flush();
    entityManager.clear();

    Company found = companyRepository.findByIdWithLeads(companyId)
            .orElseThrow(() -> new AssertionError("Company not found with id: " + companyId));

    assertThat(found.getLeads()).hasSize(3);
    for (Lead lead : found.getLeads()) {
      assertThat(lead.getCompany()).isEqualTo(found);
    }
  }
}