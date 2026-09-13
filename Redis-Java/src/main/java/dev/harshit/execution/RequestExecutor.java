package dev.harshit.execution;

public interface RequestExecutor {

    void execute(Runnable task);

    void shutdown();
}
