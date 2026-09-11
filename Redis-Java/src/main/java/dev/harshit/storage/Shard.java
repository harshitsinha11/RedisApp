package dev.harshit.storage;

import dev.harshit.core.Entry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Shard {
    private final ConcurrentHashMap<String, Entry> map = new ConcurrentHashMap<>();

    public Entry get(String key){
        return map.get(key);
    }

    public void set(String key,Entry value){
        map.put(key, value);
    }

    public void remove(String key,Entry value){
        map.remove(key,value);
    }

    public void remove(String key){
        map.remove(key);
    }

    //Returns a something like an iterable of Set<Object> where in object is {key,value}
    public Iterable<Map.Entry<String, Entry>> entries() {
        return map.entrySet();
    }
}
