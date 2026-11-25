package de.nplay.moderationbot;

import java.util.HashMap;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LRUCache<K, V> {
    private final HashMap<K, V> map;
    private final Queue<K> queue;

    private final Integer capacity;

    public LRUCache(Integer capacity) {
        this.capacity = capacity;
        map = new HashMap<>(capacity);
        queue = new ConcurrentLinkedQueue<>();
    }

    public void put(K key, V value) {
        map.put(key, value);

        if (queue.contains(key)) {
            queue.remove(key);
        } else if (queue.size() >= capacity) {
            var leastActive = queue.poll();
            map.remove(leastActive);
        }

        queue.offer(key);
    }

    public Optional<V> get(K key) {
        return Optional.ofNullable(map.get(key));
    }
}
