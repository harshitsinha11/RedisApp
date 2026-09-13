package dev.harshit.execution;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlatformThreadExecutor implements RequestExecutor {

    private final ExecutorService executor;

    public PlatformThreadExecutor(int threadCount){
        if(threadCount <=0 ) throw new IllegalArgumentException("Invalid thread count");

        this.executor = Executors.newFixedThreadPool(threadCount);
    }


    //Choose a thread from pool and start executing task
    @Override
    public void execute(Runnable task) {
        executor.submit(task);
    }

    //Stopping the threads
    @Override
    public void shutdown() {
        executor.shutdown();
    }
}
