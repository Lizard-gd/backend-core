package ru.mentee.power.crm.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import ru.mentee.power.crm.model.Company;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.repository.CompanyRepository;
import ru.mentee.power.crm.repository.LeadRepository;

@SpringBootTest
class LeadLockingServiceTest {

  @Autowired private LeadLockingService leadLockingService;

  @Autowired private LeadRepository leadRepository;

  @Autowired private CompanyRepository companyRepository;

  private UUID testLeadId;

  @BeforeEach
  void setUp() {
    leadRepository.deleteAll();
    companyRepository.deleteAll();

    Company company = new Company();
    company.setName("LockCorp");
    company.setIndustry("Testing");
    company = companyRepository.save(company);

    Lead lead = new Lead();
    lead.setFirstName("LockTest");
    lead.setEmail("locktest@example.com");
    lead.setPhone("+1234567890");
    lead.setCompany(company);
    lead.setStatus("NEW");
    lead.setCreatedAt(LocalDateTime.now());
    lead = leadRepository.save(lead);
    testLeadId = lead.getId();
  }

  @Test
  void shouldPreventLostUpdate_whenPessimisticLockUsed() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(2);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(2);

    Future<String> task1 =
        executor.submit(
            () -> {
              startLatch.await();
              Lead updated = leadLockingService.convertLeadToDealWithLock(testLeadId, "CONTACTED");
              doneLatch.countDown();
              return updated.getStatus();
            });

    Future<String> task2 =
        executor.submit(
            () -> {
              startLatch.await();
              Lead updated = leadLockingService.convertLeadToDealWithLock(testLeadId, "QUALIFIED");
              doneLatch.countDown();
              return updated.getStatus();
            });

    startLatch.countDown();
    boolean finished = doneLatch.await(10, TimeUnit.SECONDS);
    assertThat(finished).isTrue();

    String status1 = task1.get();
    String status2 = task2.get();

    assertThat(status1).isIn("CONTACTED", "QUALIFIED");
    assertThat(status2).isIn("CONTACTED", "QUALIFIED");
    assertThat(status1).isNotEqualTo(status2);
  }

  @Test
  void shouldThrowOptimisticLockException_whenConcurrentUpdateWithoutLock() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(5);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(2);

    Future<?> task1 =
        executor.submit(
            () -> {
              try {
                startLatch.await();
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
              try {
                leadLockingService.updateLeadStatusOptimistic(testLeadId, "CONTACTED");
              } finally {
                doneLatch.countDown();
              }
            });

    Future<?> task2 =
        executor.submit(
            () -> {
              try {
                startLatch.await();
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
              try {
                leadLockingService.updateLeadStatusOptimistic(testLeadId, "QUALIFIED");
              } finally {
                doneLatch.countDown();
              }
            });

    startLatch.countDown();
    boolean finished = doneLatch.await(10, TimeUnit.SECONDS);
    assertThat(finished).isTrue();

    boolean exceptionThrown = false;
    try {
      task1.get();
    } catch (ExecutionException e) {
      if (e.getCause() instanceof ObjectOptimisticLockingFailureException) {
        exceptionThrown = true;
      }
    }
    try {
      task2.get();
    } catch (ExecutionException e) {
      if (e.getCause() instanceof ObjectOptimisticLockingFailureException) {
        exceptionThrown = true;
      }
    }
    assertThat(exceptionThrown).isTrue();
  }
}
