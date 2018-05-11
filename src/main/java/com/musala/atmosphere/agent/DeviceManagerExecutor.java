// This file is part of the ATMOSPHERE mobile testing framework.
// Copyright (C) 2016 MusalaSoft
//
// ATMOSPHERE is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// ATMOSPHERE is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with ATMOSPHERE.  If not, see <http://www.gnu.org/licenses/>.

package com.musala.atmosphere.agent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
     * @return - the result of an asynchronous computation
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

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks are accepted.
     * Blocks until all tasks have completed execution, or the <b>timeout of 5 minutes</b> occurs, or the current thread
     * is interrupted, whichever happens first.
     * Use {@link #releaseResourcesAwaitTermination(long, TimeUnit)} to set the timeout.
     */
    public void releaseResourcesAwaitTermination() {
        releaseResourcesAwaitTermination(5, TimeUnit.MINUTES);
    }

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks are accepted.
     * Blocks until all tasks have completed execution, or the timeout occurs, or the current thread is interrupted,
     * whichever happens first.
     *
     * @param timeout
     *        - the maximum time to wait
     * @param unit
     *        - the time unit of the timeout argument
     */
    public void releaseResourcesAwaitTermination(long timeout, TimeUnit unit) {
        executor.shutdown();
        try {
            executor.awaitTermination(timeout, unit);
        } catch (InterruptedException e) {
            // Nothing to do here
        }
    }

    @Override
    protected void finalize() {
        releaseResources();
    }
}
