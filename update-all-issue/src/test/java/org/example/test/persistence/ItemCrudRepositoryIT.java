package org.example.test.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.example.persistence.ItemCrudRepository;
import org.example.persistence.ItemDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@MicronautTest(rollback = false, transactional = false)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ItemCrudRepositoryIT
        implements TestPropertyProvider {

    static PostgreSQLContainer postgresqlContainer = new PostgreSQLContainer();

    static {
        postgresqlContainer.start();
    }

    @Inject
    private ItemCrudRepository repository;

    public Map<String, String> getProperties() {
        return Map.ofEntries(Map.entry("datasources.default.url", postgresqlContainer.getJdbcUrl()));
    }

    /**
     * Calling update when the interceptor modifies the entity, the returned result is successfully the modified entity.
     */
    @Test
    void updateReturnsRestoredStateEntity() {
        // Given an insert and an update of the same entity
        Instant now = Instant.now();
        ItemDao itemDaoSavedInDatabase = new ItemDao("0001", "SavedInDatabase", Timestamp.from(now));
        Instant olderThanNow = now.minus(5, ChronoUnit.DAYS);
        ItemDao itemDaoNotUpdatedInDatabase = new ItemDao("0001", "NotUpdatedInDatabase", Timestamp.from(olderThanNow));

        // When calling the repository
        ItemDao saved = repository.update(itemDaoSavedInDatabase);
        ItemDao notUpdated = repository.update(itemDaoNotUpdatedInDatabase);
        Optional<ItemDao> select = repository.findById(itemDaoSavedInDatabase.getId());

        // Then the returned entity by the second update has the initial values and the transient field updated
        assertEquals("SavedInDatabase", saved.getName());
        assertEquals("SavedInDatabase", notUpdated.getName());
        assertEquals("SavedInDatabase", select.get().getName());
        assertFalse(saved.getModificationSkipped());
        assertFalse(select.get().getModificationSkipped());
        assertTrue(notUpdated.getModificationSkipped());
    }

    /**
     * Calling update when the interceptor modifies the entity, the returned result is successfully the modified entity.
     */
    @Test
    void updateNewerEventTimeUpdateEntity() {
        // Given an insert and an update of the same entity
        Instant now = Instant.now();
        ItemDao itemDaoSavedInDatabase = new ItemDao("0002", "SavedInDatabase", Timestamp.from(now));
        Instant newerThanNow = now.plus(5, ChronoUnit.DAYS);
        ItemDao itemDaoUpdatedInDatabase = new ItemDao("0002", "UpdatedInDatabase", Timestamp.from(newerThanNow));

        // When calling the repository
        ItemDao saved = repository.update(itemDaoSavedInDatabase);
        ItemDao updated = repository.update(itemDaoUpdatedInDatabase);
        Optional<ItemDao> select = repository.findById(itemDaoSavedInDatabase.getId());

        // Then the returned entity by the second update has the initial values and the transient field updated
        assertEquals("SavedInDatabase", saved.getName());
        assertEquals("UpdatedInDatabase", updated.getName());
        assertEquals("UpdatedInDatabase", select.get().getName());
        assertFalse(saved.getModificationSkipped());
        assertFalse(updated.getModificationSkipped());
        assertFalse(select.get().getModificationSkipped());
    }

    /**
     * Calling updateAll when the interceptor modifies the entity, the returned result is not the modified entity but the initial entity.
     */
    @Test
    void updateAllNotReturnRestoredStateEntity() {
// Given an insert and an update of the same entity
        Instant now = Instant.now();
        ItemDao itemDaoSavedInDatabase = new ItemDao("0003", "SavedInDatabase", Timestamp.from(now));
        Instant olderThanNow = now.minus(5, ChronoUnit.DAYS);
        ItemDao itemDaoNotUpdatedInDatabase = new ItemDao("0003", "NotUpdatedInDatabase", Timestamp.from(olderThanNow));

        // When calling the repository
        Iterable<ItemDao> saved = repository.updateAll(List.of(itemDaoSavedInDatabase));
        Iterable<ItemDao> notUpdated = repository.updateAll(List.of(itemDaoNotUpdatedInDatabase));
        Optional<ItemDao> select = repository.findById(itemDaoSavedInDatabase.getId());

        // Then the returned entity by the second update has the initial values and the transient field updated
        assertEquals("SavedInDatabase", saved.iterator().next().getName());
        assertEquals("SavedInDatabase", notUpdated.iterator().next().getName());
        assertEquals("SavedInDatabase", select.get().getName());
        assertFalse(saved.iterator().next().getModificationSkipped());
        assertFalse(select.get().getModificationSkipped());
        assertTrue(notUpdated.iterator().next().getModificationSkipped());
    }

    /**
     * Calling updateAll when the interceptor modifies the entity, the returned result is not the modified entity but the initial entity.
     */
    @Test
    void updateAllNewerEventTimeUpdateEntity() {
// Given an insert and an update of the same entity
        Instant now = Instant.now();
        ItemDao itemDaoSavedInDatabase = new ItemDao("0004", "SavedInDatabase", Timestamp.from(now));
        Instant newerThanNow = now.plus(5, ChronoUnit.DAYS);
        ItemDao itemDaoUpdatedInDatabase = new ItemDao("0004", "UpdatedInDatabase", Timestamp.from(newerThanNow));

        // When calling the repository
        Iterable<ItemDao> saved = repository.updateAll(List.of(itemDaoSavedInDatabase));
        Iterable<ItemDao> updated = repository.updateAll(List.of(itemDaoUpdatedInDatabase));
        Optional<ItemDao> select = repository.findById(itemDaoSavedInDatabase.getId());

        // Then the returned entity by the second update has the initial values and the transient field updated
        assertEquals("SavedInDatabase", saved.iterator().next().getName());
        assertEquals("UpdatedInDatabase", updated.iterator().next().getName());
        assertEquals("UpdatedInDatabase", select.get().getName());
        assertFalse(saved.iterator().next().getModificationSkipped());
        assertFalse(select.get().getModificationSkipped());
        assertFalse(updated.iterator().next().getModificationSkipped());
    }
}
