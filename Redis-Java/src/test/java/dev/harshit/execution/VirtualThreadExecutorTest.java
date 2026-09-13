package dev.harshit.execution;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class VirtualThreadExecutorTest {

    @Test
    void shouldExecuteTaskOnVirtualThread()
            throws InterruptedException {

        RequestExecutor executor =
                new VirtualThreadExecutor();

        CountDownLatch latch = new CountDownLatch(1);

        executor.execute(() -> {

            assertTrue(
                    Thread.currentThread().isVirtual()
            );

            latch.countDown();
        });

        assertTrue(
                latch.await(2, TimeUnit.SECONDS)
        );

        executor.shutdown();
    }

    @Test
    void shouldExecuteMultipleTasks()
            throws InterruptedException {

        RequestExecutor executor =
                new VirtualThreadExecutor();

        CountDownLatch latch = new CountDownLatch(100);

        for (int i = 0; i < 100; i++) {
            executor.execute(latch::countDown);
        }

        assertTrue(
                latch.await(2, TimeUnit.SECONDS)
        );

        executor.shutdown();
    }
}