package ru.mentee.power.crm.spring.mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.spring.dto.generated.CreateLeadRequest;
import ru.mentee.power.crm.spring.dto.generated.LeadResponse;
import ru.mentee.power.crm.spring.dto.generated.UpdateLeadRequest;

@Mapper(componentModel = "spring")
public interface LeadMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "company", ignore = true)
  @Mapping(target = "status", ignore = true)
  Lead toEntity(CreateLeadRequest request);

  @Mapping(
      target = "company",
      expression = "java(lead.getCompany() != null ? lead.getCompany().getName() : null)")
  LeadResponse toResponse(Lead lead);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "company", ignore = true)
  @Mapping(target = "status", ignore = true)
  void updateEntity(UpdateLeadRequest request, @MappingTarget Lead lead);

  default OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
    if (localDateTime == null) {
      return null;
    }
    return localDateTime.atOffset(ZoneOffset.UTC);
  }
}
