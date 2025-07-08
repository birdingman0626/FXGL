/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */
package com.almasb.fxgl.core.reflect;

import com.almasb.fxgl.core.util.EmptyRunnable;
import com.almasb.fxgl.logging.Logger;

import java.lang.invoke.MethodHandle;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * FFC is a wrapper around a native library, allowing
 * calls to native functions as if they were Java functions.
 *
 * Each FFC has its own single thread that executes all call functions.
 *
 * Note: In Java 17, the Foreign Function & Memory API is not available,
 * so this implementation provides compatibility stubs that log warnings
 * and return default values.
 *
 * @author Almas Baim (https://github.com/AlmasB)
 */
public final class ForeignFunctionCaller {

    private static final Logger log = Logger.get(ForeignFunctionCaller.class);

    private static final AtomicInteger threadCount = new AtomicInteger(0);

    private List<Path> libraries;
    private ForeignFunctionContext context;
    public BlockingQueue<Consumer<ForeignFunctionContext>> executionQueue = new ArrayBlockingQueue<>(1000);
    private AtomicBoolean isRunning = new AtomicBoolean(true);

    private Thread thread;
    private CountDownLatch loadedLatch = new CountDownLatch(1);

    private Runnable onLoaded = EmptyRunnable.INSTANCE;
    private Runnable onUnloaded = EmptyRunnable.INSTANCE;
    private boolean isLoaded = false;

    /**
     * For a given library, only 1 FFC can be created.
     *
     * @param libraries the list of files to load as libraries
     */
    public ForeignFunctionCaller(List<Path> libraries) {
        this.libraries = new ArrayList<>(libraries);
    }

    public void setOnLoaded(Runnable onLoaded) {
        this.onLoaded = onLoaded;
    }

    public void setOnUnloaded(Runnable onUnloaded) {
        this.onUnloaded = onUnloaded;
    }

    public void load() {
        if (isLoaded) {
            log.warning("Already loaded: " + libraries);
            return;
        }

        isLoaded = true;

        thread = new Thread(this::threadTask, "FFCThread-" + threadCount.getAndIncrement());
        thread.setDaemon(true);
        thread.start();

        // Wait until libs are loaded and loop entered
        try {
            loadedLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warning("Interrupted while waiting for libraries to load", e);
        }
    }

    private void threadTask() {
        log.debug("Starting native setup task");
        
        // Java 17 compatibility: FFM API is not available in stable form
        // This is experimental/WIP code that requires Java 21+ FFM API
        log.warning("Foreign Function & Memory API is not available in Java 17. GL functionality disabled.");
        
        context = new ForeignFunctionContext();
        
        log.debug("Java 17 compatibility context created (GL disabled)");
        onLoaded.run();
        
        // Signal that loading is complete
        loadedLatch.countDown();

        while (isRunning.get()) {
            try {
                var functionCall = executionQueue.take();
                functionCall.accept(context);
            } catch (Exception e) {
                log.warning("Native call failed", e);
            }
        }

        onUnloaded.run();
    }

    private MethodHandle getFunctionImpl(String name, Object fd) {
        // Java 17 compatibility: return null handle
        log.warning("Foreign Function calls are not supported in Java 17. Function: " + name);
        return null;
    }

    private Object callImpl(String name, Object fd, Object... args) {
        // Java 17 compatibility: return default values
        log.warning("Foreign Function calls are not supported in Java 17. Function: " + name);
        return 0; // Default return value
    }

    public void execute(Consumer<ForeignFunctionContext> functionCall) {
        if (!isLoaded) {
            log.warning("Libraries are not loaded, call load() first");
            return;
        }

        try {
            executionQueue.put(functionCall);
        } catch (Throwable e) {
            log.warning("Failed to schedule a function call", e);
        }
    }

    public void unload() {
        unload(context -> {});
    }

    /**
     * @param libExitFunctionCall the last function to call in the loaded library(-ies),
     * do not schedule any other execute() operations within the call
     */
    public void unload(Consumer<ForeignFunctionContext> libExitFunctionCall) {
        if (!isLoaded) {
            log.warning("Libraries are not loaded, ignoring unload request");
            return;
        }

        execute(context -> {
            libExitFunctionCall.accept(context);
            isRunning.set(false);
        });
        
        isLoaded = false;
    }

    public final class ForeignFunctionContext {

        public ForeignFunctionContext() {
            // Java 17 compatibility constructor
        }

        public Object getArena() {
            log.warning("Arena not available in Java 17");
            return null;
        }

        public Object getLinker() {
            log.warning("Linker not available in Java 17");
            return null;
        }

        public List<Object> getLookups() {
            log.warning("SymbolLookup not available in Java 17");
            return new ArrayList<>();
        }

        public MethodHandle getFunction(String name, Object fd) {
            return getFunctionImpl(name, fd);
        }

        public Object call(String name, Object fd, Object... args) {
            return callImpl(name, fd, args);
        }

        public void callVoidNoArg(String name) {
            callImpl(name, null);
        }

        public Object allocateIntArray(int length) {
            log.warning("MemorySegment allocation not available in Java 17");
            return null;
        }

        public Object allocateCharArrayFrom(String s) {
            log.warning("MemorySegment allocation not available in Java 17");
            return null;
        }
    }
}
