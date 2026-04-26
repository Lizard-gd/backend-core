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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.repository.LeadRepository;

@ExtendWith(MockitoExtension.class)
public class LeadServiceMockTest {
  @Mock
  private LeadRepository mockRepository;

  private LeadService service;

  @BeforeEach
  @Test
  void setUp() {
    service = new LeadService(mockRepository);
  }

  @Test
  void shouldCallRepositorySave_whenAddingNewLead() {
    when(mockRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    Lead result = service.addLead("TestUser", "test@example.com",
            "+123456789", "Test Corp", "NEW");

    verify(mockRepository, times(1)).save(any(Lead.class));
    assertThat(result.email()).isEqualTo("test@example.com");
    assertThat(result.company()).isEqualTo("Test Corp");
  }

  @Test
  void shouldNotCallSave_whenEmailExists() {
    Lead existingLead = new Lead("123", "Existing",
            "existing@example.com", "+777", "Company",
            "NEW", LocalDateTime.now());
    when(mockRepository.findByEmail("existing@example.com"))
          .thenReturn(Optional.of(existingLead));

    assertThatThrownBy(() ->
          service.addLead("Existing", "existing@example.com",
                  "+888", "Other", "NEW")
          ).isInstanceOf(IllegalStateException.class);

    verify(mockRepository, never()).save(any(Lead.class));
  }

  @Test
  void shouldCallFindByEmailBeforeSave() {
    when(mockRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    service.addLead("OrderUser", "order@example.com", "+999", "Order Corp", "NEW");

    InOrder inOrder = inOrder(mockRepository);
    inOrder.verify(mockRepository).findByEmail("order@example.com");
    inOrder.verify(mockRepository).save(any(Lead.class));
  }
}
