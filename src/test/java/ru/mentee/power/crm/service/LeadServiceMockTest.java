package ru.mentee.power.crm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.repository.CompanyRepository;
import ru.mentee.power.crm.repository.DealRepository;
import ru.mentee.power.crm.repository.LeadRepository;
import ru.mentee.power.crm.spring.client.EmailValidationFeignClient;
import ru.mentee.power.crm.spring.client.EmailValidationResponse;

@ExtendWith(MockitoExtension.class)
public class LeadServiceMockTest {

  @Mock private LeadRepository mockRepository;
  @Mock private DealRepository mockDealRepository;
  @Mock private LeadProcessor mockLeadProcessor;
  @Mock private CompanyRepository mockCompanyRepository;
  @Mock private EmailValidationFeignClient mockEmailValidationClient;

  private LeadService service;

  @BeforeEach
  void setUp() {
    service =
        new LeadService(
            mockRepository,
            mockDealRepository,
            mockLeadProcessor,
            mockCompanyRepository,
            mockEmailValidationClient);
  }

  @Test
  void shouldCallRepositorySave_whenAddingNewLead() {
    String email = "test@example.com";
    EmailValidationResponse validResponse = new EmailValidationResponse(email, true, "OK");
    when(mockEmailValidationClient.validateEmail(email)).thenReturn(validResponse);
    when(mockRepository.findByEmailNative(email)).thenReturn(Optional.empty());
    when(mockRepository.save(any(Lead.class)))
        .thenAnswer(invocation -> invocation.getArgument(0)); // возвращаем переданный объект

    Lead result = service.addLead("TestUser", email, "+123456789", "NEW");

    verify(mockRepository, times(1)).save(any(Lead.class));
    assertThat(result).isNotNull();
    assertThat(result.getEmail()).isEqualTo(email);
    assertThat(result.getCompany()).isNull();
  }

  @Test
  void shouldNotCallSave_whenEmailExists() {
    String email = "existing@example.com";
    EmailValidationResponse validResponse = new EmailValidationResponse(email, true, "OK");
    when(mockEmailValidationClient.validateEmail(email)).thenReturn(validResponse);

    Lead existingLead = new Lead();
    existingLead.setId(UUID.randomUUID());
    existingLead.setEmail(email);
    when(mockRepository.findByEmailNative(email)).thenReturn(Optional.of(existingLead));

    assertThatThrownBy(() -> service.addLead("Existing", email, "+888", "NEW"))
        .isInstanceOf(IllegalStateException.class);

    verify(mockRepository, never()).save(any(Lead.class));
  }

  @Test
  void shouldCallFindByEmailBeforeSave() {
    String email = "order@example.com";
    EmailValidationResponse validResponse = new EmailValidationResponse(email, true, "OK");
    when(mockEmailValidationClient.validateEmail(email)).thenReturn(validResponse);
    when(mockRepository.findByEmailNative(email)).thenReturn(Optional.empty());
    when(mockRepository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));

    service.addLead("OrderUser", email, "+999", "NEW");

    InOrder inOrder = inOrder(mockRepository);
    inOrder.verify(mockRepository).findByEmailNative(email);
    inOrder.verify(mockRepository).save(any(Lead.class));
  }
}
