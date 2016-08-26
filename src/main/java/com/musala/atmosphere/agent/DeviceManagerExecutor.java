package com.musala.atmosphere.agent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Used to executes {@link Runnable} tasks in new threads, created by {@link ExecutorService}.
 * 
 * @author denis.bialev
 *
 */
public class DeviceManagerExecutor {
    private final ExecutorService executor;

    /**
     * Creates a thread pool that creates new threads as needed, but will reuse previously constructed threads when they
     * are available.
     */
    DeviceManagerExecutor() {
        executor = Executors.newCachedThreadPool();
    }

    /**
     * Executes the runnable task in new thread.
     * 
     * @param task
     *        - a task that will be executed
     */
    public Future<?> execute(Runnable task) {
        Future<?> future = executor.submit(task);
        return future;
    }

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be
     * accepted.
     */
    public void releaseResources() {
        executor.shutdown();
    }

    @Override
    protected void finalize() {
        releaseResources();
    }
}
