package dev.harshit.execution;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VirtualThreadExecutor implements RequestExecutor{

    public final ExecutorService executor;

    public VirtualThreadExecutor(){
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }


    @Override
    public void execute(Runnable task) {
        executor.submit(task);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }
}
