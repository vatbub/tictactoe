package com.github.vatbub.tictactoe.view;

/*-
 * #%L
 * tictactoe
 * %%
 * Copyright (C) 2016 - 2017 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import javafx.application.Platform;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * A {@code ScheduledThreadPoolExecutor} for javaFX animations
 */
@SuppressWarnings("JavaDoc")
public class AnimationThreadPoolExecutor extends ScheduledThreadPoolExecutor{
    /**
     * Animations submitted using {@link #submitWaitForUnlock(Runnable)}, {@link #submitWaitForUnlock(Runnable, Object)} or {@link #submitWaitForUnlock(Callable)} will wait for this value to be false before being executed
     */
    private boolean blocked;

    /**
     * Creates a new {@code ScheduledThreadPoolExecutor} with the
     * given core pool size.
     *
     * @param corePoolSize the number of threads to keep in the pool, even
     *                     if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     */
    @SuppressWarnings("unused")
    public AnimationThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    /**
     * Creates a new {@code ScheduledThreadPoolExecutor} with the
     * given initial parameters.
     *
     * @param corePoolSize  the number of threads to keep in the pool, even
     *                      if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param threadFactory the factory to use when the executor
     *                      creates a new thread
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     * @throws NullPointerException     if {@code threadFactory} is null
     */
    @SuppressWarnings("unused")
    public AnimationThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    /**
     * Creates a new ScheduledThreadPoolExecutor with the given
     * initial parameters.
     *
     * @param corePoolSize the number of threads to keep in the pool, even
     *                     if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param handler      the handler to use when execution is blocked
     *                     because the thread bounds and queue capacities are reached
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     * @throws NullPointerException     if {@code handler} is null
     */
    @SuppressWarnings("unused")
    public AnimationThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
    }

    /**
     * Creates a new ScheduledThreadPoolExecutor with the given
     * initial parameters.
     *
     * @param corePoolSize  the number of threads to keep in the pool, even
     *                      if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param threadFactory the factory to use when the executor
     *                      creates a new thread
     * @param handler       the handler to use when execution is blocked
     *                      because the thread bounds and queue capacities are reached
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     * @throws NullPointerException     if {@code threadFactory} or
     *                                  {@code handler} is null
     */
    @SuppressWarnings("unused")
    public AnimationThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
    }

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */
    @NotNull
    @Override
    public Future<?> submit(Runnable task) {
        Runnable effectiveTask = () -> Platform.runLater(task);
        return super.schedule(effectiveTask, 0, NANOSECONDS);
    }

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */
    @NotNull
    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        Runnable effectiveTask = () -> Platform.runLater(task);
        return super.schedule(Executors.callable(effectiveTask, result), 0, NANOSECONDS);
    }

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */
    @NotNull
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        Callable<T> effectiveTask = () -> {
            FutureTask<T> effectiveCall = new FutureTask<>(task);
            Platform.runLater(effectiveCall);
            return effectiveCall.get();
        };

        return super.schedule(effectiveTask, 0, NANOSECONDS);
    }

    @SuppressWarnings("UnusedReturnValue")
    public Future<?> submitWaitForUnlock(Runnable task) {
        Runnable effectiveTask = () -> {
            // PrintStreams magically don't make the wait loop hang
            PrintStream nullStream = new PrintStream(new OutputStream() {
                public void write(int b) {
                    //DO NOTHING
                }
            });
            while (isBlocked()){
                nullStream.println("Waiting...");
            }
            // run
            Platform.runLater(task);
        };
        return super.schedule(effectiveTask, 0, NANOSECONDS);
    }

    public <T> Future<T> submitWaitForUnlock(Runnable task, T result) {
        Runnable effectiveTask = () -> {
            // PrintStreams magically don't make the wait loop hang
            PrintStream nullStream = new PrintStream(new OutputStream() {
                public void write(int b) {
                    //DO NOTHING
                }
            });
            while (isBlocked()){
                nullStream.println("Waiting...");
            }
            // run
            Platform.runLater(task);
        };
        return super.schedule(Executors.callable(effectiveTask, result), 0, NANOSECONDS);
    }

    public <T> Future<T> submitWaitForUnlock(Callable<T> task) {
        Callable<T> effectiveTask = () -> {
            // PrintStreams magically don't make the wait loop hang
            PrintStream nullStream = new PrintStream(new OutputStream() {
                public void write(int b) {
                    //DO NOTHING
                }
            });
            while (isBlocked()){
                nullStream.println("Waiting...");
            }
            // run
            FutureTask<T> effectiveCall = new FutureTask<>(task);
            Platform.runLater(effectiveCall);
            return effectiveCall.get();
        };

        return super.schedule(effectiveTask, 0, NANOSECONDS);
    }

    public boolean isBlocked() {
        return blocked;
    }

    /**
     * If {@code true}, Animations submitted using {@link #submitWaitForUnlock(Runnable)}, {@link #submitWaitForUnlock(Runnable, Object)} or {@link #submitWaitForUnlock(Callable)} will wait until {@code setBlocked(false} is called.
     * @param blocked The new value for blocked
     */
    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
