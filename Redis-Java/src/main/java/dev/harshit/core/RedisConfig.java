package dev.harshit.core;

import dev.harshit.execution.ExecutorType;

public record RedisConfig(
        int port,
        int shards,
        int threads,
        ExecutorType executor,
        long cleanerIntervalMs
) { }
