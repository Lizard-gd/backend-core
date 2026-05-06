package ru.mentee.power.crm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mentee.power.crm.model.Deal;
import ru.mentee.power.crm.model.DealStatus;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.repository.DealRepository;
import ru.mentee.power.crm.repository.LeadRepository;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

  @Mock
  private DealRepository dealRepository;

  @Mock
  private LeadRepository leadRepository;

  private DealService dealService;

  @BeforeEach
  void setUp() {
    dealService = new DealService(dealRepository, leadRepository);
  }

  @Test
  void convertLeadToDeal_shouldCreateDeal_whenLeadExistsAndQualified() {
    String leadId = "lead-123";
    Lead qualifiedLead = new Lead(leadId, "John", "john@example.com",
              "+123456789", "TechCorp", "QUALIFIED", LocalDateTime.now());
    BigDecimal amount = new BigDecimal("100000");

    when(leadRepository.findById(leadId)).thenReturn(Optional.of(qualifiedLead));

    Deal createdDeal = dealService.convertLeadToDeal(leadId, amount);

    assertThat(createdDeal).isNotNull();
    assertThat(createdDeal.getLeadId()).isEqualTo(leadId);
    assertThat(createdDeal.getAmount()).isEqualTo(amount);
    assertThat(createdDeal.getStatus()).isEqualTo(DealStatus.NEW);

    verify(dealRepository).save(any(Deal.class));
  }

  @Test
  void convertLeadToDeal_shouldThrowException_whenLeadNotFound() {
    String leadId = "non-existent";
    when(leadRepository.findById(leadId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> dealService.convertLeadToDeal(leadId, BigDecimal.TEN))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Lead not found");

    verify(dealRepository, never()).save(any());
  }

  @Test
  void convertLeadToDeal_shouldThrowException_whenLeadNotQualified() {
    String leadId = "lead-456";
    Lead nonQualifiedLead = new Lead(leadId, "Jane", "jane@example.com",
              "+987654321", "DesignStudio", "NEW", LocalDateTime.now());
    when(leadRepository.findById(leadId)).thenReturn(Optional.of(nonQualifiedLead));

    assertThatThrownBy(() -> dealService.convertLeadToDeal(leadId, new BigDecimal("5000")))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Only QUALIFIED leads");

    verify(dealRepository, never()).save(any());
  }

  @Test
  void transitionDealStatus_shouldUpdateStatus_whenValidTransition() {
    String dealId = "deal-123";
    Deal deal = new Deal(dealId, "lead-1",
            BigDecimal.valueOf(1000), DealStatus.NEW, LocalDateTime.now());
    when(dealRepository.findById(dealId)).thenReturn(Optional.of(deal));

    Deal updated = dealService.transitionDealStatus(dealId, DealStatus.QUALIFIED);

    assertThat(updated.getStatus()).isEqualTo(DealStatus.QUALIFIED);
    verify(dealRepository).save(deal);
  }

  @Test
  void transitionDealStatus_shouldThrowException_whenTransitionInvalid() {
    String dealId = "deal-456";
    Deal deal = new Deal(dealId, "lead-2",
            BigDecimal.valueOf(2000), DealStatus.WON, LocalDateTime.now());
    when(dealRepository.findById(dealId)).thenReturn(Optional.of(deal));

    assertThatThrownBy(() -> dealService.transitionDealStatus(dealId, DealStatus.NEW))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Cannot transition from WON to NEW");

    verify(dealRepository, never()).save(any());
  }

  @Test
  void transitionDealStatus_shouldThrowException_whenDealNotFound() {
    String nonExistentId = "no-such-deal";
    when(dealRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> dealService.transitionDealStatus(nonExistentId, DealStatus.QUALIFIED))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Deal not found");
  }
}