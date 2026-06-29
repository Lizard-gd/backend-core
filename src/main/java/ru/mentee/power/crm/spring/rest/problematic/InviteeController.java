package ru.mentee.power.crm.spring.rest.problematic;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.mentee.power.crm.domain.Invitee;
import ru.mentee.power.crm.spring.repository.InviteeRepository;

/**
 * ЗАДАНИЕ: Найдите все проблемы в этом контроллере используя чек-лист. Ожидается найти минимум 10
 * проблем из разных категорий.
 */
@RestController
public class InviteeController {

  @Autowired InviteeRepository repository;

  // 1 TODO: Студент должен найти проблемы в этом методе +
  @PostMapping("/getInvitees")
  public List<Invitee> getInvitees() {
    return repository.findAll();
  }

  // 2 TODO: Студент должен найти проблемы в этом методе +
  @GetMapping("/invitees/{id}")
  public Invitee getById(@PathVariable UUID id) {
    return repository.findById(id).orElse(null);
  }

  // 3,4 TODO: Студент должен найти проблемы в этом методе +
  @PostMapping("/invitees")
  public Invitee create(@RequestBody Map<String, Object> params) {
    String email = (String) params.get("email");
    String firstName = (String) params.get("firstName");

    // Проверка email через SQL
    String sql = "SELECT COUNT(*) FROM invitees WHERE email = '" + email + "'";
    // repository.executeNativeQuery(sql); // Представим что это выполняется

    Invitee invitee = new Invitee();
    invitee.setId(UUID.randomUUID());
    invitee.setEmail(email);
    invitee.setFirstName(firstName);
    invitee.setCreatedAt(Instant.now());

    return repository.save(invitee);
  }

  // 5 TODO: Студент должен найти проблемы в этом методе
  @DeleteMapping("/invitees/{id}")
  public Invitee delete(@PathVariable UUID id) {
    Invitee invitee = repository.findById(id).orElse(null);
    if (invitee != null) {
      repository.delete(invitee);
    }
    return invitee;
  }

  // 6,7 TODO: Студент должен найти проблемы в этом методе
  @PutMapping("/invitees/{id}/status")
  public Invitee updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
    try {
      Invitee invitee = repository.findById(id).orElseThrow();
      String status = body.get("status");

      // Бизнес-логика в контроллере
      if (status.equals("ACTIVE") || status.equals("INACTIVE")) {
        invitee.setStatus(status);
      } else {
        throw new RuntimeException("Invalid status");
      }

      return repository.save(invitee);
    } catch (Exception e) {
      // Пустой catch
      return null;
    }
  }
}
