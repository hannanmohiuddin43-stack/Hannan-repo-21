package com.example.demo.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryStore<K, V> {

    private final ConcurrentMap<K, V> entries = new ConcurrentHashMap<>();

    public void save(K key, V value) {
        entries.put(key, value);
    }

    public Optional<V> replaceIfPresent(K key, V value) {
        return Optional.ofNullable(entries.computeIfPresent(key, (existingKey, existingValue) -> value));
    }

    public Optional<V> find(K key) {
        return Optional.ofNullable(entries.get(key));
    }

    public List<V> findAll() {
        return new ArrayList<>(entries.values());
    }

    public boolean delete(K key) {
        return entries.remove(key) != null;
    }
}
