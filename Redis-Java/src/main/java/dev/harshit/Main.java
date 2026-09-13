package dev.harshit;

import dev.harshit.core.ConfigLoader;
import dev.harshit.core.RedisConfig;
import dev.harshit.core.RedisEngine;
import dev.harshit.expiration.ExpirationCleaner;
import dev.harshit.execution.PlatformThreadExecutor;
import dev.harshit.execution.RequestExecutor;
import dev.harshit.execution.VirtualThreadExecutor;
import dev.harshit.tcp.TcpServer;

public final class Main {

    public static void main(String[] args) {

        // Load configuration
        RedisConfig config = ConfigLoader.load();

        // Create core storage engine
        RedisEngine engine = new RedisEngine(config.shards()); //takes shardCount

        // Start active expiration cleaner
        ExpirationCleaner cleaner = new ExpirationCleaner(
                        engine.getStore(), //An instance of ShardedStore - same for everyone
                        config.cleanerIntervalMs() //Wait time
                );

        // A sperate thread for cleaner
        Thread cleanerThread = new Thread(cleaner, "expiration-cleaner");
        cleanerThread.start();

        // Create request executor
        RequestExecutor executor;

        //Choosing Implementation
        if (config.executor().name().equals("PLATFORM")) {
            executor = new PlatformThreadExecutor(config.threads());
        } else {
            executor = new VirtualThreadExecutor();
        }

        // Create and start TCP server
        TcpServer server = new TcpServer(
                config.port(), // Our listening port
                engine, //Redis
                executor //Multithreading implementation
        );

        Thread serverThread = new Thread(
                server, //Separate thread working on taking commands from sockets(connections)
                "tcp-server"
        );
        serverThread.start();

        System.out.println("Redis server started on port " + config.port());
    }
}