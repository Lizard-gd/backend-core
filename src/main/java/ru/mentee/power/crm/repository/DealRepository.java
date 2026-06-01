package ru.mentee.power.crm.repository;

import java.util.List;
import java.util.Optional;
import ru.mentee.power.crm.model.Deal;
import ru.mentee.power.crm.model.DealStatus;

public interface DealRepository {
  void save(Deal deal);

  Optional<Deal> findById(String id);

  List<Deal> findAll();

  List<Deal> findByStatus(DealStatus status);

  void deleteById(String id);
}
