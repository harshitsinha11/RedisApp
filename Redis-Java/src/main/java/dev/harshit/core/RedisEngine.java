package dev.harshit.core;

import dev.harshit.api.Redis;
import dev.harshit.storage.ShardedStore;

public final class RedisEngine implements Redis {

    private final ShardedStore store;

    //Initializing shards array containing obj
    public RedisEngine(int shardCount) {
        this.store = new ShardedStore(shardCount);
    }

    @Override
    public String get(String key) {

        Entry entry = store.getValue(key);

        if (entry == null) return null;

        // Lazy expiration - Remove and return null
        if (entry.isExpired()) {
            store.removeValue(key, entry);
            return null;
        }

        return entry.value();
    }

    @Override
    public void set(String key, String value) {
        store.setValue(key, new Entry(value));
    }

    @Override
    public void set(String key, String value, long ttl) {

        if (ttl <= 0) {
            throw new IllegalArgumentException("TTL must be greater than 0");
        }

        store.setValue(key, new Entry(value, ttl));
    }

    @Override
    public long ttl(String key) {

        Entry entry = store.getValue(key);

        //Entry with given key not present
        if (entry == null) return -2;

        //Entry with given key expired
        if (entry.isExpired()) {
            store.removeValue(key, entry); //We are avoiding race condition for now
            return -2;
        }

        //Entry has no expiry
        if (entry.expiresAt() == Entry.NO_EXP) return -1;

        //Calculate remaining time (in nanosecond)
        long remainingNanos = entry.expiresAt() - System.nanoTime();

        //Works similar to Math.ciel
        return (remainingNanos + 999_999_999L) / 1_000_000_000L;
    }
}