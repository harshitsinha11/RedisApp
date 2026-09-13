package dev.harshit.expiration;

import dev.harshit.core.Entry;
import dev.harshit.storage.Shard;
import dev.harshit.storage.ShardedStore;

import java.util.Map;


public class ExpirationCleaner implements Runnable{

    private final ShardedStore store;
    private final long intervalMs; //Wait time before cleaner starts again

    private volatile boolean running = true;


    public ExpirationCleaner(ShardedStore shards, long intervalMs) {
        if(intervalMs < 0) throw new RuntimeException("Cleaner interval < 0");

        this.store = shards;
        this.intervalMs = intervalMs;
    }

    @Override
    public void run() {
        while (running) {

            cleanExpiredEntries();

            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void cleanExpiredEntries(){
        //Iterate through all the store
        for(Shard shard : store.getShards()){

            //Iterate through all the key-value pairs per shard(map)
            for(Map.Entry<String,Entry> entry : shard.entries()){

                Entry value = entry.getValue();

                //Delete if expired
                if(value.isExpired()) shard.remove(entry.getKey(), value);

            }
        }
    }

    public void stop(){
        running = false;
    }
}
