package ru.mentee.power.crm.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Repository<T> {

  void add(T entity);

  void remove(UUID id);

  Optional<T> findById(UUID id);

  List<T> findAll();
}