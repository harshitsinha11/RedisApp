package dev.harshit.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class RedisEngineTest {

    /*
     * ---------------------------------------------------------
     * BASIC SET / GET
     * ---------------------------------------------------------
     */

    @Test
    void shouldSetAndGetValue() {

        RedisEngine engine = new RedisEngine(4);

        engine.set("A", "hello");

        assertEquals("hello", engine.get("A"));
    }

    @Test
    void shouldReturnNullForMissingKey() {

        RedisEngine engine = new RedisEngine(4);

        assertNull(engine.get("does-not-exist"));
    }

    @Test
    void shouldOverwriteExistingValue() {

        RedisEngine engine = new RedisEngine(4);

        engine.set("A", "first");
        engine.set("A", "second");

        assertEquals("second", engine.get("A"));
    }

    @Test
    void shouldStoreDifferentKeysIndependently() {

        RedisEngine engine = new RedisEngine(4);

        engine.set("A", "one");
        engine.set("B", "two");
        engine.set("C", "three");

        assertEquals("one", engine.get("A"));
        assertEquals("two", engine.get("B"));
        assertEquals("three", engine.get("C"));
    }


    /*
     * ---------------------------------------------------------
     * NO-EXPIRATION BEHAVIOUR
     * ---------------------------------------------------------
     */

    @Test
    void shouldStoreKeyWithoutExpiration() {

        RedisEngine engine = new RedisEngine(4);

        engine.set("A", "hello");

        assertEquals("hello", engine.get("A"));
    }

    @Test
    void ttlShouldReturnMinusOneForKeyWithoutExpiration() {

        RedisEngine engine = new RedisEngine(4);

        engine.set("A", "hello");

        assertEquals(-1, engine.ttl("A"));
    }


    /*
     * ---------------------------------------------------------
     * TTL
     * ---------------------------------------------------------
     */

    @Test
    void shouldSetValueWithTtl() {

        RedisEngine engine = new RedisEngine(4);

        engine.set("A", "hello", 5);

        assertEquals("hello", engine.get("A"));
    }

    @Test
    void ttlShouldReturnPositiveValueForExpiringKey() {

        RedisEngine engine = new RedisEngine(4);

        engine.set("A", "hello", 5);

        long ttl = engine.ttl("A");

        assertTrue(ttl >= 1 && ttl <= 5);
    }

    @Test
    void ttlShouldDecreaseOverTime() throws InterruptedException {

        RedisEngine engine = new RedisEngine(4);

        engine.set("A", "hello", 3);

        long firstTtl = engine.ttl("A");

        Thread.sleep(1100);

        long secondTtl = engine.ttl("A");

        assertTrue(secondTtl < firstTtl);
    }


    /*
     * ---------------------------------------------------------
     * EXPIRATION
     * ---------------------------------------------------------
     */

    @Test
    void shouldExpireKeyAfterTtl() throws InterruptedException {

        RedisEngine engine = new RedisEngine(4);

        engine.set("A", "hello", 1);

        assertEquals("hello", engine.get("A"));

        Thread.sleep(1100);

        assertNull(engine.get("A"));
    }

    @Test
    void expiredKeyShouldReturnMinusTwoFromTtl()
            throws InterruptedException {

        RedisEngine engine = new RedisEngine(4);

        engine.set("A", "hello", 1);

        Thread.sleep(1100);

        assertEquals(-2, engine.ttl("A"));
    }

    @Test
    void missingKeyShouldReturnMinusTwoFromTtl() {

        RedisEngine engine = new RedisEngine(4);

        assertEquals(-2, engine.ttl("missing"));
    }


    /*
     * ---------------------------------------------------------
     * OVERWRITE + TTL
     * ---------------------------------------------------------
     */

    @Test
    void settingWithoutTtlShouldRemovePreviousExpiration()
            throws InterruptedException {

        RedisEngine engine = new RedisEngine(4);

        engine.set("A", "old", 1);

        engine.set("A", "new");

        Thread.sleep(1100);

        assertEquals("new", engine.get("A"));
        assertEquals(-1, engine.ttl("A"));
    }

    @Test
    void settingNewTtlShouldReplaceOldTtl() {

        RedisEngine engine = new RedisEngine(4);

        engine.set("A", "first", 10);
        engine.set("A", "second", 5);

        assertEquals("second", engine.get("A"));

        long ttl = engine.ttl("A");

        assertTrue(ttl >= 1 && ttl <= 5);
    }

    @Test
    void expiredOldValueShouldNotDeleteNewValue()
            throws InterruptedException {

        RedisEngine engine = new RedisEngine(4);

        engine.set("A", "old", 1);

        Thread.sleep(1100);

        engine.set("A", "new");

        assertEquals("new", engine.get("A"));
    }


    /*
     * ---------------------------------------------------------
     * INVALID TTL
     * ---------------------------------------------------------
     */

    @Test
    void shouldRejectZeroTtl() {

        RedisEngine engine = new RedisEngine(4);

        assertThrows(
                IllegalArgumentException.class,
                () -> engine.set("A", "hello", 0)
        );
    }

    @Test
    void shouldRejectNegativeTtl() {

        RedisEngine engine = new RedisEngine(4);

        assertThrows(
                IllegalArgumentException.class,
                () -> engine.set("A", "hello", -1)
        );
    }


    /*
     * ---------------------------------------------------------
     * EMPTY STRINGS
     * ---------------------------------------------------------
     */

    @Test
    void shouldSupportEmptyValue() {

        RedisEngine engine = new RedisEngine(4);

        engine.set("A", "");

        assertEquals("", engine.get("A"));
    }

    @Test
    void shouldSupportEmptyKey() {

        RedisEngine engine = new RedisEngine(4);

        engine.set("", "hello");

        assertEquals("hello", engine.get(""));
    }


    /*
     * ---------------------------------------------------------
     * MANY KEYS / SHARDING
     * ---------------------------------------------------------
     */

    @Test
    void shouldStoreManyKeys() {

        RedisEngine engine = new RedisEngine(16);

        for (int i = 0; i < 10_000; i++) {
            engine.set("key-" + i, "value-" + i);
        }

        for (int i = 0; i < 10_000; i++) {
            assertEquals(
                    "value-" + i,
                    engine.get("key-" + i)
            );
        }
    }


    /*
     * ---------------------------------------------------------
     * CONCURRENT READS
     * ---------------------------------------------------------
     */

    @Test
    void shouldSupportConcurrentReads() throws Exception {

        RedisEngine engine = new RedisEngine(16);

        engine.set("A", "hello");

        int threadCount = 16;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {

            futures.add(
                    executor.submit(
                            () -> engine.get("A")
                    )
            );
        }

        for (Future<String> future : futures) {
            assertEquals("hello", future.get());
        }

        executor.shutdown();
    }


    /*
     * ---------------------------------------------------------
     * CONCURRENT WRITES
     * ---------------------------------------------------------
     */

    @Test
    void shouldSupportConcurrentWrites() throws Exception {

        RedisEngine engine = new RedisEngine(16);

        int threadCount = 16;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {

            int threadId = i;

            futures.add(
                    executor.submit(
                            () -> engine.set(
                                    "key-" + threadId,
                                    "value-" + threadId
                            )
                    )
            );
        }

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();

        for (int i = 0; i < threadCount; i++) {

            assertEquals(
                    "value-" + i,
                    engine.get("key-" + i)
            );
        }
    }


    /*
     * ---------------------------------------------------------
     * CONCURRENT SAME-KEY ACCESS
     * ---------------------------------------------------------
     */

    @Test
    void shouldHandleConcurrentUpdatesToSameKey()
            throws Exception {

        RedisEngine engine = new RedisEngine(16);

        int threadCount = 20;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch start =
                new CountDownLatch(1);

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {

            int threadId = i;

            futures.add(
                    executor.submit(() -> {

                        start.await();

                        engine.set(
                                "A",
                                "value-" + threadId
                        );

                        return null;
                    })
            );
        }

        start.countDown();

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();

        String finalValue = engine.get("A");

        assertNotNull(finalValue);
        assertTrue(finalValue.startsWith("value-"));
    }


    /*
     * ---------------------------------------------------------
     * CONCURRENT GET + SET
     * ---------------------------------------------------------
     */

    @Test
    void shouldSupportConcurrentGetAndSet()
            throws Exception {

        RedisEngine engine = new RedisEngine(16);

        engine.set("A", "initial");

        int threadCount = 20;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch start =
                new CountDownLatch(1);

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {

            int threadId = i;

            futures.add(
                    executor.submit(() -> {

                        start.await();

                        if (threadId % 2 == 0) {
                            engine.get("A");
                        } else {
                            engine.set(
                                    "A",
                                    "value-" + threadId
                            );
                        }

                        return null;
                    })
            );
        }

        start.countDown();

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();

        assertNotNull(engine.get("A"));
    }
}