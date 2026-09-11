package dev.harshit.core;

public final class Entry {

    public static final long NO_EXP = -1L; //NO expiry

    private final String value;
    private final long expiresAt; //Expiry value in nanoseconds


    public Entry(String value) {
        this.value = value;
        this.expiresAt = NO_EXP;
    }

    public Entry(String value, long ttlSeconds) {
        this.value = value;

        //Negative ttl inputs
        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException("TTL must be positive");
        }

        this.expiresAt = ttlSeconds * 1_000_000_000L + System.nanoTime();
    }

    //getValue
    public String value(){
        return value;
    }

    //getExpiry
    public long expiresAt(){
        return expiresAt;
    }

    //checkExpiry - return true if expired
    public boolean isExpired(){
        return expiresAt != NO_EXP && expiresAt <= System.nanoTime();
    }
}
