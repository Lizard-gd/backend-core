package ru.mentee.power.crm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.repository.DealRepository;
import ru.mentee.power.crm.repository.LeadRepository;

@ExtendWith(MockitoExtension.class)
public class LeadServiceMockTest {

  @Mock
  private LeadRepository mockRepository;

  @Mock
  private DealRepository mockDealRepository;

  @Mock
  private LeadProcessor mockLeadProcessor;

  private LeadService service;

  @BeforeEach
  void setUp() {
    service = new LeadService(mockRepository, mockDealRepository, mockLeadProcessor);
  }

  @Test
  void shouldCallRepositorySave_whenAddingNewLead() {
    when(mockRepository.findByEmailNative(anyString())).thenReturn(Optional.empty());

    Lead result = service.addLead("TestUser", "test@example.com",
            "+123456789", "Test Corp", "NEW");

    verify(mockRepository, times(1)).save(any(Lead.class));
    // Используем геттеры вместо record-методов
    assertThat(result.getEmail()).isEqualTo("test@example.com");
    assertThat(result.getCompany()).isEqualTo("Test Corp");
  }

  @Test
  void shouldNotCallSave_whenEmailExists() {
    // Создаём существующего лида через сеттеры
    Lead existingLead = new Lead();
    existingLead.setId(UUID.randomUUID());
    existingLead.setFirstName("Existing");
    existingLead.setEmail("existing@example.com");
    existingLead.setPhone("+777");
    existingLead.setCompany("Company");
    existingLead.setStatus("NEW");
    existingLead.setCreatedAt(LocalDateTime.now());

    when(mockRepository.findByEmailNative("existing@example.com"))
            .thenReturn(Optional.of(existingLead));

    assertThatThrownBy(() ->
            service.addLead("Existing", "existing@example.com",
                    "+888", "Other", "NEW")
    ).isInstanceOf(IllegalStateException.class);

    verify(mockRepository, never()).save(any(Lead.class));
  }

  @Test
  void shouldCallFindByEmailBeforeSave() {
    when(mockRepository.findByEmailNative(anyString())).thenReturn(Optional.empty());

    service.addLead("OrderUser", "order@example.com", "+999", "Order Corp", "NEW");

    InOrder inOrder = inOrder(mockRepository);
    inOrder.verify(mockRepository).findByEmailNative("order@example.com");
    inOrder.verify(mockRepository).save(any(Lead.class));
  }
}