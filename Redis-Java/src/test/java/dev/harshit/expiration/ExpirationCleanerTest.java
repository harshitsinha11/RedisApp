package dev.harshit.expiration;

import dev.harshit.core.Entry;
import dev.harshit.storage.ShardedStore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExpirationCleanerTest {

    @Test
    void shouldRemoveExpiredEntries() throws InterruptedException {

        ShardedStore store = new ShardedStore(4);

        store.setValue("A", new Entry("hello", 1));

        ExpirationCleaner cleaner =
                new ExpirationCleaner(store, 100);

        Thread cleanerThread = new Thread(cleaner);
        cleanerThread.start();

        Thread.sleep(1500);

        assertNull(store.getValue("A"));

        cleaner.stop();
        cleanerThread.interrupt();
    }

    @Test
    void shouldNotRemoveNonExpiredEntries()
            throws InterruptedException {

        ShardedStore store = new ShardedStore(4);

        store.setValue("A", new Entry("hello", 3));

        ExpirationCleaner cleaner =
                new ExpirationCleaner(store, 100);

        Thread cleanerThread = new Thread(cleaner);
        cleanerThread.start();

        Thread.sleep(500);

        assertNotNull(store.getValue("A"));

        cleaner.stop();
        cleanerThread.interrupt();
    }

    @Test
    void shouldNotRemoveNonExpiringEntries()
            throws InterruptedException {

        ShardedStore store = new ShardedStore(4);

        store.setValue("A", new Entry("hello"));

        ExpirationCleaner cleaner =
                new ExpirationCleaner(store, 100);

        Thread cleanerThread = new Thread(cleaner);
        cleanerThread.start();

        Thread.sleep(500);

        assertNotNull(store.getValue("A"));

        cleaner.stop();
        cleanerThread.interrupt();
    }
}