package dev.harshit.execution;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class PlatformThreadExecutorTest {

    @Test
    void shouldExecuteTask() throws InterruptedException {

        RequestExecutor executor =
                new PlatformThreadExecutor(2);

        CountDownLatch latch = new CountDownLatch(1);

        executor.execute(latch::countDown);

        assertTrue(
                latch.await(2, TimeUnit.SECONDS)
        );

        executor.shutdown();
    }

    @Test
    void shouldExecuteMultipleTasks()
            throws InterruptedException {

        RequestExecutor executor =
                new PlatformThreadExecutor(2);

        CountDownLatch latch = new CountDownLatch(5);

        for (int i = 0; i < 5; i++) {
            executor.execute(latch::countDown);
        }

        assertTrue(
                latch.await(2, TimeUnit.SECONDS)
        );

        executor.shutdown();
    }

    @Test
    void shouldRejectInvalidThreadCount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new PlatformThreadExecutor(0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new PlatformThreadExecutor(-1)
        );
    }
}