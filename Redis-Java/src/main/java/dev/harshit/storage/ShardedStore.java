package dev.harshit.storage;

import dev.harshit.core.Entry;

public class ShardedStore {

    private final Shard[] shards;


    public ShardedStore(int shardCount) {
        if (shardCount <= 0) {
            throw new IllegalArgumentException("shardCount must be greater than 0");
        }

        this.shards = new Shard[shardCount];

        for(int i=0; i<shardCount; i++){
            shards[i] = new Shard();
        }
    }

    //Quite similar to a load balancer chooses a shard [0,n)
    private Shard getShard(String key){
        int hash = key.hashCode();

        //returns [0,n) - always same for any string
        int index = Math.floorMod(hash, shards.length);

        //return ref of that shard obj
        return shards[index];
    }

    //Simple getter setters after choosing shard using getShard()
    public Entry getValue(String key){
        return getShard(key).get(key);
    }

    public void setValue(String key, Entry entry){
        getShard(key).set(key,entry);
    }

    //Exposing shards
    //ExpirationCleaner will need its access
    public Shard[] getShards(){
        return shards;
    }
}
