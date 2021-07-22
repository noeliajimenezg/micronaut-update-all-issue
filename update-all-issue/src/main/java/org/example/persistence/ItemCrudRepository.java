package org.example.persistence;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

@Repository
public interface ItemCrudRepository extends CrudRepository<ItemDao, String> {
}
