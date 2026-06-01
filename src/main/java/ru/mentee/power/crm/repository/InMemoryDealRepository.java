package ru.mentee.power.crm.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import ru.mentee.power.crm.model.Deal;
import ru.mentee.power.crm.model.DealStatus;

@Repository
public class InMemoryDealRepository implements DealRepository {
  private final Map<String, Deal> storage = new ConcurrentHashMap<>();

  @Override
  public void save(Deal deal) {
    storage.put(deal.getId(), deal);
  }

  @Override
  public Optional<Deal> findById(String id) {
    return Optional.ofNullable(storage.get(id));
  }

  @Override
  public List<Deal> findAll() {
    return new ArrayList<>(storage.values());
  }

  @Override
  public List<Deal> findByStatus(DealStatus status) {
    return storage.values().stream()
        .filter(deal -> deal.getStatus() == status)
        .collect(Collectors.toList());
  }

  @Override
  public void deleteById(String id) {
    storage.remove(id);
  }
}
