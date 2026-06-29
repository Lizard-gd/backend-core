package ru.mentee.power.crm.spring.rest.fixed;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InviteeRepository extends JpaRepository<Invitee, UUID> {
  boolean existsByEmail(String email);
}
