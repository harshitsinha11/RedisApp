package dev.harshit.core;

import dev.harshit.execution.ExecutorType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

//To create and return RedisConfig obj and return with values from redis.properties
public final class ConfigLoader {

    private ConfigLoader() {}

    public static RedisConfig load() {

        Properties properties = new Properties();

        //Try with resources - inputStream obj data
        try (InputStream input = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("redis.properties")) {

            if (input == null) {
                throw new IllegalStateException("redis.properties not found");
            }

            properties.load(input);

        } catch (IOException e) {
            throw new IllegalStateException("Failed to load redis.properties", e);
        }

        return new RedisConfig(
                Integer.parseInt(properties.getProperty("server.port")), //No need in our scope
                Integer.parseInt(properties.getProperty("server.shards")),
                Integer.parseInt(properties.getProperty("server.threads")),
                ExecutorType.valueOf(properties.getProperty("server.executor").toUpperCase()), //Testing both threads
                Long.parseLong(properties.getProperty("cleaner.interval-ms"))
        );
    }
}