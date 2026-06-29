package ru.mentee.power.crm.spring.rest.fixed;

import org.springframework.stereotype.Component;

@Component
public class InviteeMapper {

  public Invitee toEntity(CreateInviteeRequest request) {
    Invitee invitee = new Invitee();
    invitee.setEmail(request.getEmail());
    invitee.setFirstName(request.getFirstName());
    return invitee;
  }

  public InviteeResponse toResponse(Invitee invitee) {
    return new InviteeResponse(
        invitee.getId(),
        invitee.getEmail(),
        invitee.getFirstName(),
        invitee.getStatus(),
        invitee.getCreatedAt());
  }
}
