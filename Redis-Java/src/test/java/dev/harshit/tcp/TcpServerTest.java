package dev.harshit.tcp;

import dev.harshit.core.RedisEngine;
import dev.harshit.execution.PlatformThreadExecutor;
import dev.harshit.execution.RequestExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class TcpServerTest {

    private static final int PORT = 19001;

    private RedisEngine engine;
    private RequestExecutor executor;
    private TcpServer server;
    private ExecutorService serverExecutor;

    @BeforeEach
    void setUp() throws InterruptedException {

        engine = new RedisEngine(4);

        executor = new PlatformThreadExecutor(4);

        server = new TcpServer(
                PORT,
                engine,
                executor
        );

        serverExecutor =
                Executors.newSingleThreadExecutor();

        serverExecutor.submit(server);

        // Give the server a moment to start listening.
        Thread.sleep(100);
    }

    @AfterEach
    void tearDown() {

        server.stop();

        executor.shutdown();

        serverExecutor.shutdownNow();
    }


    @Test
    void shouldSetAndGetValue() throws IOException {

        try (Socket socket =
                     new Socket("localhost", PORT)) {

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()
                            )
                    );

            PrintWriter writer =
                    new PrintWriter(
                            socket.getOutputStream(),
                            true
                    );

            writer.println("SET A hello");

            assertEquals(
                    "OK",
                    reader.readLine()
            );

            writer.println("GET A");

            assertEquals(
                    "hello",
                    reader.readLine()
            );
        }
    }


    @Test
    void shouldReturnNullForMissingKey()
            throws IOException {

        try (Socket socket =
                     new Socket("localhost", PORT)) {

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()
                            )
                    );

            PrintWriter writer =
                    new PrintWriter(
                            socket.getOutputStream(),
                            true
                    );

            writer.println("GET missing");

            assertEquals(
                    "NULL",
                    reader.readLine()
            );
        }
    }


    @Test
    void shouldHandleTtl() throws IOException {

        try (Socket socket =
                     new Socket("localhost", PORT)) {

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()
                            )
                    );

            PrintWriter writer =
                    new PrintWriter(
                            socket.getOutputStream(),
                            true
                    );

            writer.println("SET A hello 5");

            assertEquals(
                    "OK",
                    reader.readLine()
            );

            writer.println("TTL A");

            long ttl =
                    Long.parseLong(reader.readLine());

            assertTrue(
                    ttl >= 1 && ttl <= 5
            );
        }
    }


    @Test
    void shouldHandleMultipleCommandsOnSameConnection()
            throws IOException {

        try (Socket socket =
                     new Socket("localhost", PORT)) {

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()
                            )
                    );

            PrintWriter writer =
                    new PrintWriter(
                            socket.getOutputStream(),
                            true
                    );

            writer.println("SET A hello");

            assertEquals(
                    "OK",
                    reader.readLine()
            );

            writer.println("GET A");

            assertEquals(
                    "hello",
                    reader.readLine()
            );

            writer.println("TTL A");

            assertEquals(
                    "-1",
                    reader.readLine()
            );
        }
    }


    @Test
    void shouldHandleMultipleConnections()
            throws Exception {

        try (
                Socket socket1 =
                        new Socket("localhost", PORT);

                Socket socket2 =
                        new Socket("localhost", PORT)
        ) {

            BufferedReader reader1 =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket1.getInputStream()
                            )
                    );

            PrintWriter writer1 =
                    new PrintWriter(
                            socket1.getOutputStream(),
                            true
                    );

            BufferedReader reader2 =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket2.getInputStream()
                            )
                    );

            PrintWriter writer2 =
                    new PrintWriter(
                            socket2.getOutputStream(),
                            true
                    );


            writer1.println("SET A first");

            assertEquals(
                    "OK",
                    reader1.readLine()
            );


            writer2.println("SET B second");

            assertEquals(
                    "OK",
                    reader2.readLine()
            );


            writer1.println("GET A");

            assertEquals(
                    "first",
                    reader1.readLine()
            );


            writer2.println("GET B");

            assertEquals(
                    "second",
                    reader2.readLine()
            );
        }
    }


    @Test
    void shouldRejectInvalidCommand()
            throws IOException {

        try (Socket socket =
                     new Socket("localhost", PORT)) {

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()
                            )
                    );

            PrintWriter writer =
                    new PrintWriter(
                            socket.getOutputStream(),
                            true
                    );

            writer.println("INVALID A");

            assertEquals(
                    "ERROR unknown command",
                    reader.readLine()
            );
        }
    }


    @Test
    void shouldRejectInvalidArguments()
            throws IOException {

        try (Socket socket =
                     new Socket("localhost", PORT)) {

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()
                            )
                    );

            PrintWriter writer =
                    new PrintWriter(
                            socket.getOutputStream(),
                            true
                    );

            writer.println("GET");

            assertEquals(
                    "ERROR wrong number of arguments GET",
                    reader.readLine()
            );
        }
    }

    @Test
    void shouldHandleManyConcurrentConnections() throws Exception {

        int connectionCount = 50;

        ExecutorService clients =
                Executors.newFixedThreadPool(connectionCount);

        CountDownLatch start =
                new CountDownLatch(1);

        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < connectionCount; i++) {

            int clientId = i;

            futures.add(
                    clients.submit(() -> {

                        start.await();

                        try (Socket socket =
                                     new Socket("localhost", PORT)) {

                            BufferedReader reader =
                                    new BufferedReader(
                                            new InputStreamReader(
                                                    socket.getInputStream()
                                            )
                                    );

                            PrintWriter writer =
                                    new PrintWriter(
                                            socket.getOutputStream(),
                                            true
                                    );

                            String key = "client-" + clientId;
                            String value = "value-" + clientId;

                            // SET
                            writer.println(
                                    "SET " + key + " " + value
                            );

                            if (!"OK".equals(reader.readLine())) {
                                return false;
                            }

                            // GET
                            writer.println(
                                    "GET " + key
                            );

                            return value.equals(reader.readLine());
                        }
                    })
            );
        }

        // Release all clients at approximately the same time.
        start.countDown();

        for (Future<Boolean> future : futures) {
            assertTrue(future.get());
        }

        clients.shutdown();
    }
}