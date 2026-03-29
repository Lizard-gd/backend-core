package ru.mentee.power.crm.infrastructure;

import ru.mentee.power.crm.domain.Lead;
import ru.mentee.power.crm.domain.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InMemoryLeadRepository implements Repository<Lead> {
  private final List<Lead> storage = new ArrayList<>();

  @Override
    public void add(Lead entity) {
    if (!storage.contains(entity)) {
      storage.add(entity);
    }
  }

  @Override
    public void remove(UUID id) {
    storage.removeIf(lead -> lead.id().equals(id));
  }

  @Override
    public Optional<Lead> findById(UUID id) {
    return storage.stream() // превращаем список в поток
                .filter(lead -> lead.id().equals(id)) // оставляем только нужный
                .findFirst(); // берём первый
  }

  @Override
    public List<Lead> findAll() {
    return new ArrayList<>(storage); // Создаём копию storage, а не просто делаем return.
        // Так клиент не удалит исходник
  }
}
