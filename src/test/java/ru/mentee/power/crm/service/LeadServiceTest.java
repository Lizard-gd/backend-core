package ru.mentee.power.crm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.mentee.power.crm.model.Company;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.repository.CompanyRepository;
import ru.mentee.power.crm.repository.DealRepository;
import ru.mentee.power.crm.repository.LeadRepository;
import ru.mentee.power.crm.spring.client.EmailValidationFeignClient;
import ru.mentee.power.crm.spring.client.EmailValidationResponse;
import ru.mentee.power.crm.spring.exception.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

  @Mock private LeadRepository repository;
  @Mock private DealRepository dealRepository;
  @Mock private LeadProcessor leadProcessor;
  @Mock private CompanyRepository companyRepository;
  @Mock private EmailValidationFeignClient emailValidationClient;

  @InjectMocks private LeadService service;

  private Company createCompany(String name) {
    Company company = new Company();
    company.setId(UUID.randomUUID());
    company.setName(name);
    return company;
  }

  private EmailValidationResponse validResponse(String email) {
    return new EmailValidationResponse(email, true, "OK");
  }

  @Test
  void shouldCreateLead_whenEmailIsUnique() {
    String email = "unique@example.com";
    when(emailValidationClient.validateEmail(email)).thenReturn(validResponse(email));
    when(repository.findByEmailNative(anyString())).thenReturn(Optional.empty());
    when(repository.save(any(Lead.class)))
        .thenAnswer(
            invocation -> {
              Lead lead = invocation.getArgument(0);
              if (lead.getId() == null) {
                lead.setId(UUID.randomUUID());
              }
              return lead;
            });

    Lead result = service.addLead("Ivan", email, "+123456789", "NEW");

    assertThat(result).isNotNull();
    assertThat(result.getFirstName()).isEqualTo("Ivan");
    assertThat(result.getEmail()).isEqualTo(email);
    assertThat(result.getPhone()).isEqualTo("+123456789");
    assertThat(result.getCompany()).isNull();
    assertThat(result.getStatus()).isEqualTo("NEW");
    assertThat(result.getId()).isNotNull();

    ArgumentCaptor<Lead> captor = ArgumentCaptor.forClass(Lead.class);
    verify(repository).save(captor.capture());
    Lead savedLead = captor.getValue();
    assertThat(savedLead.getId()).isNotNull();
    assertThat(savedLead.getFirstName()).isEqualTo("Ivan");
  }

  @Test
  void shouldThrowException_whenEmailAlreadyExists() {
    String email = "duplicate@example.com";
    when(emailValidationClient.validateEmail(email)).thenReturn(validResponse(email));

    Lead existing = new Lead();
    existing.setEmail(email);
    when(repository.findByEmailNative(email)).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> service.addLead("Ivan", email, "111111", "NEW"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Lead with email already exists");

    verify(repository, never()).save(any());
  }

  @Test
  void shouldFindAllLeads() {
    when(repository.findAll()).thenReturn(List.of(new Lead(), new Lead()));
    List<Lead> result = service.findAll();
    assertThat(result).hasSize(2);
    verify(repository).findAll();
  }

  @Test
  void shouldFindLeadById() {
    UUID id = UUID.randomUUID();
    Lead lead = new Lead();
    lead.setId(id);
    when(repository.findById(id)).thenReturn(Optional.of(lead));

    Optional<Lead> result = service.findById(id);
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(id);
  }

  @Test
  void shouldReturnEmpty_whenLeadNotFoundById() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());
    Optional<Lead> result = service.findById(id);
    assertThat(result).isEmpty();
  }

  @Test
  void shouldFindLeadByEmail() {
    String email = "search@example.com";
    Lead lead = new Lead();
    lead.setEmail(email);
    when(repository.findByEmailNative(email)).thenReturn(Optional.of(lead));

    Optional<Lead> result = service.findByEmail(email);
    assertThat(result).isPresent();
    assertThat(result.get().getEmail()).isEqualTo(email);
  }

  @Test
  void shouldReturnEmpty_whenLeadNotFoundByEmail() {
    when(repository.findByEmailNative(anyString())).thenReturn(Optional.empty());
    Optional<Lead> result = service.findByEmail("notfound@example.com");
    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnOnlyNewLeads_whenFindByStatusNew() {
    when(repository.findByStatusNative("NEW"))
        .thenReturn(List.of(createLeadWithStatus("NEW"), createLeadWithStatus("NEW")));
    List<Lead> result = service.findByStatus("NEW");
    assertThat(result).hasSize(2);
    assertThat(result).allMatch(lead -> lead.getStatus().equals("NEW"));
  }

  @Test
  void shouldUpdateExistingLead() {
    UUID id = UUID.randomUUID();
    Lead existing = new Lead();
    existing.setId(id);
    existing.setFirstName("John");
    existing.setCreatedAt(LocalDateTime.now());

    Company newCompany = createCompany("NewCorp");

    Lead updatedLead = new Lead();
    updatedLead.setFirstName("Johnny");
    updatedLead.setEmail("john@update.com");
    updatedLead.setPhone("+111");
    updatedLead.setCompany(newCompany);
    updatedLead.setStatus("QUALIFIED");

    when(repository.findById(id)).thenReturn(Optional.of(existing));
    when(repository.save(any(Lead.class))).thenAnswer(inv -> inv.getArgument(0));

    Lead result = service.update(id, updatedLead);

    assertThat(result.getFirstName()).isEqualTo("Johnny");
    assertThat(result.getCompany()).isNotNull();
    assertThat(result.getCompany().getName()).isEqualTo("NewCorp");
    assertThat(result.getStatus()).isEqualTo("QUALIFIED");
    assertThat(result.getCreatedAt()).isEqualTo(existing.getCreatedAt());

    verify(repository).save(any(Lead.class));
  }

  @Test
  void shouldThrowWhenUpdateNonExistingLead() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    Lead dummy = new Lead();
    assertThatThrownBy(() -> service.update(id, dummy))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Lead not found with id: " + id);
  }

  @Test
  void shouldDeleteExistingLead() {
    UUID id = UUID.randomUUID();
    Lead lead = new Lead();
    lead.setId(id);
    when(repository.findById(id)).thenReturn(Optional.of(lead));
    doNothing().when(repository).deleteById(id);

    service.delete(id);
    verify(repository).deleteById(id);
  }

  @Test
  void shouldThrowWhenDeleteNonExistingLead() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.delete(id))
        .isInstanceOf(ResponseStatusException.class)
        .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void findLeads_ShouldFilterBySearchAndStatusAndDate() {
    Lead lead1 =
        createLeadWithFields("Alice", "alice@test.com", "NEW", LocalDateTime.now().minusDays(1));
    Lead lead2 = createLeadWithFields("Bob", "bob@test.com", "QUALIFIED", LocalDateTime.now());
    when(repository.findAll()).thenReturn(List.of(lead1, lead2));

    List<Lead> result = service.findLeads("Alice", "NEW", null, null);
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getFirstName()).isEqualTo("Alice");
  }

  private Lead createLeadWithStatus(String status) {
    Lead l = new Lead();
    l.setStatus(status);
    return l;
  }

  private Lead createLeadWithFields(
      String firstName, String email, String status, LocalDateTime createdAt) {
    Lead l = new Lead();
    l.setFirstName(firstName);
    l.setEmail(email);
    l.setStatus(status);
    l.setCreatedAt(createdAt);
    return l;
  }

  @Test
  void getLeadById_shouldReturnLead_whenExists() {
    UUID id = UUID.randomUUID();
    Lead expectedLead = new Lead();
    expectedLead.setId(id);
    expectedLead.setFirstName("Existing");
    when(repository.findById(id)).thenReturn(Optional.of(expectedLead));

    Lead result = service.getLeadById(id);

    assertThat(result).isEqualTo(expectedLead);
    verify(repository).findById(id);
  }

  @Test
  void getLeadById_shouldThrowEntityNotFoundException_whenNotFound() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getLeadById(id))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Lead not found with id: " + id);
  }

  @Test
  void updateLeadOrThrow_shouldUpdateAndReturnLead_whenExists() {
    UUID id = UUID.randomUUID();
    Lead existingLead = new Lead();
    existingLead.setId(id);
    existingLead.setFirstName("Old");
    existingLead.setEmail("old@test.com");
    existingLead.setPhone("+000");
    existingLead.setStatus("NEW");

    Lead updatedLead = new Lead();
    updatedLead.setFirstName("New");
    updatedLead.setEmail("new@test.com");
    updatedLead.setPhone("+111");
    updatedLead.setStatus("QUALIFIED");
    Company company = new Company();
    company.setId(UUID.randomUUID());
    updatedLead.setCompany(company);

    when(repository.findById(id)).thenReturn(Optional.of(existingLead));
    when(repository.save(any(Lead.class))).thenAnswer(inv -> inv.getArgument(0));

    Lead result = service.updateLeadOrThrow(id, updatedLead);

    assertThat(result.getFirstName()).isEqualTo("New");
    assertThat(result.getEmail()).isEqualTo("new@test.com");
    assertThat(result.getPhone()).isEqualTo("+111");
    assertThat(result.getStatus()).isEqualTo("QUALIFIED");
    assertThat(result.getCompany()).isEqualTo(company);
    verify(repository).save(existingLead);
  }

  @Test
  void updateLeadOrThrow_shouldThrowEntityNotFoundException_whenNotFound() {
    UUID id = UUID.randomUUID();
    Lead updatedLead = new Lead();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.updateLeadOrThrow(id, updatedLead))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Lead not found with id: " + id);
  }

  @Test
  void deleteLeadOrThrow_shouldDelete_whenExists() {
    UUID id = UUID.randomUUID();
    when(repository.existsById(id)).thenReturn(true);
    doNothing().when(repository).deleteById(id);

    service.deleteLeadOrThrow(id);

    verify(repository).existsById(id);
    verify(repository).deleteById(id);
  }

  @Test
  void deleteLeadOrThrow_shouldThrowEntityNotFoundException_whenNotFound() {
    UUID id = UUID.randomUUID();
    when(repository.existsById(id)).thenReturn(false);

    assertThatThrownBy(() -> service.deleteLeadOrThrow(id))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Lead not found with id: " + id);
  }

  @Test
  void updateLead_shouldReturnUpdatedLead_whenLeadExists() {
    UUID leadId = UUID.randomUUID();
    Lead existingLead = new Lead();
    existingLead.setId(leadId);
    existingLead.setFirstName("Old");
    existingLead.setEmail("old@test.com");
    existingLead.setPhone("+1111111111");
    existingLead.setStatus("NEW");

    Lead updatedLeadData = new Lead();
    updatedLeadData.setFirstName("New");
    updatedLeadData.setEmail("new@test.com");
    updatedLeadData.setPhone("+2222222222");
    updatedLeadData.setStatus("QUALIFIED");

    when(repository.findById(leadId)).thenReturn(Optional.of(existingLead));
    when(repository.save(any(Lead.class))).thenAnswer(inv -> inv.getArgument(0));

    Optional<Lead> result = service.updateLead(leadId, updatedLeadData);

    assertThat(result).isPresent();
    Lead updatedLead = result.get();
    assertThat(updatedLead.getId()).isEqualTo(leadId);
    assertThat(updatedLead.getFirstName()).isEqualTo("New");
    assertThat(updatedLead.getEmail()).isEqualTo("new@test.com");
    assertThat(updatedLead.getPhone()).isEqualTo("+2222222222");
    assertThat(updatedLead.getStatus()).isEqualTo("QUALIFIED");

    verify(repository).findById(leadId);
    verify(repository).save(existingLead);
  }

  @Test
  void updateLead_shouldReturnEmptyOptional_whenLeadDoesNotExist() {
    UUID leadId = UUID.randomUUID();
    Lead updatedLeadData = new Lead();

    when(repository.findById(leadId)).thenReturn(Optional.empty());

    Optional<Lead> result = service.updateLead(leadId, updatedLeadData);

    assertThat(result).isEmpty();
    verify(repository).findById(leadId);
    verify(repository, never()).save(any());
  }

  @Test
  void deleteLead_shouldReturnTrue_whenLeadExists() {
    UUID leadId = UUID.randomUUID();
    when(repository.existsById(leadId)).thenReturn(true);
    doNothing().when(repository).deleteById(leadId);

    boolean result = service.deleteLead(leadId);

    assertThat(result).isTrue();
    verify(repository).existsById(leadId);
    verify(repository).deleteById(leadId);
  }

  @Test
  void deleteLead_shouldReturnFalse_whenLeadDoesNotExist() {
    UUID leadId = UUID.randomUUID();
    when(repository.existsById(leadId)).thenReturn(false);

    boolean result = service.deleteLead(leadId);

    assertThat(result).isFalse();
    verify(repository).existsById(leadId);
    verify(repository, never()).deleteById(any());
  }
}
