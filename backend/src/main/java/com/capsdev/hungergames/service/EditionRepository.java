package com.capsdev.hungergames.service;

import com.capsdev.hungergames.seed.EditionData;
import com.capsdev.hungergames.seed.SeedData;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.UnaryOperator;

/** Stockage en mémoire, sûr pour l'accès concurrent (le batch simulé écrit pendant que l'API lit). */
@Repository
public class EditionRepository {

    private final Map<Long, EditionData> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence;

    public EditionRepository(SeedData seedData) {
        long max = 0;
        for (EditionData data : seedData.editions()) {
            store.put(data.edition().id(), data);
            max = Math.max(max, data.edition().id());
        }
        this.sequence = new AtomicLong(max);
    }

    public long nextId() {
        return sequence.incrementAndGet();
    }

    public Collection<EditionData> findAll() {
        return store.values();
    }

    public Optional<EditionData> find(long id) {
        return Optional.ofNullable(store.get(id));
    }

    public void save(EditionData data) {
        store.put(data.edition().id(), data);
    }

    public void update(long id, UnaryOperator<EditionData> change) {
        store.computeIfPresent(id, (key, current) -> change.apply(current));
    }
}
