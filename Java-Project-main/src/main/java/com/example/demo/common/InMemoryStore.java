package com.example.demo.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryStore<K, V> {

    private final Map<K, V> entries = new HashMap<>();

    public void save(K key, V value) {
        entries.put(key, value);
    }

    public V find(K key) {
        return entries.get(key);
    }

    public List<V> findAll() {
        return new ArrayList<>(entries.values());
    }

    public void delete(K key) {
        entries.remove(key);
    }
}
