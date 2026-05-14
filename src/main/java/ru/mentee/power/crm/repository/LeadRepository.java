package ru.mentee.power.crm.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.mentee.power.crm.model.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeadRepository extends JpaRepository<Lead, UUID> {
  @Query(value = "SELECT * FROM leads WHERE email = ?1", nativeQuery = true)
  Optional<Lead> findByEmailNative(String email);

  @Query(value = "SELECT * FROM leads WHERE status = ?1", nativeQuery = true)
  List<Lead> findByStatusNative(String status);

  Optional<Lead> findByEmail(String email);

  List<Lead> findByCompany(String company);

  long countByStatus(String status);

  boolean existsByEmail(String email);

  List<Lead> findByStatusAndCompany(String status, String company);

  List<Lead> findByStatusOrderByCreatedAtDesc(String status);

  @Query("SELECT l FROM Lead l WHERE l.status IN :statuses")
  List<Lead> findByStatusIn(@Param("statuses") List<String> statuses);

  @Query("SELECT l FROM Lead l WHERE l.createdAt > :date")
  List<Lead> findCreatedAfter(@Param("date") java.time.LocalDateTime date);

  Page<Lead> findAll(Pageable pageable);

  Page<Lead> findByStatus(String status, Pageable pageable);

  Page<Lead> findByCompany(String company, Pageable pageable);

  @Query("SELECT l FROM Lead l WHERE l.status IN :statuses")
  Page<Lead> findByStatusInPaged(@Param("statuses") List<String> statuses, Pageable pageable);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE Lead l SET l.status = :newStatus WHERE l.status = :oldStatus")
  int updateStatusBulk(@Param("oldStatus") String oldStatus, @Param("newStatus") String newStatus);

  @Modifying(clearAutomatically = true)
  @Query("DELETE FROM Lead l WHERE l.status = :status")
  int deleteByStatusBulk(@Param("status") String status);
}
