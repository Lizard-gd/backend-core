package ru.mentee.power.crm.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.mentee.power.crm.model.Company;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

  @EntityGraph(attributePaths = {"leads"})
  @Query("SELECT c FROM Company c WHERE c.id = :id")
  Optional<Company> findByIdWithLeads(@Param("id") UUID id);

  Optional<Company> findByName(String name);
}