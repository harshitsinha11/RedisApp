package dev.harshit.api;

public interface Redis {

    String get(String key);

    void set(String key, String value);

    void set(String key, String value, long ttl);

    long ttl(String key);
}
