package com.github.fmcejudo.tracing.generator.service;

import com.github.fmcejudo.tracing.generator.builder.TraceBuilder;
import com.github.fmcejudo.tracing.generator.exporter.Exporter;
import com.github.fmcejudo.tracing.generator.task.Task;
import io.vavr.control.Try;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TraceGenerator implements Closeable {

    private static final int NUMBER_OF_THREADS = 3;

    private final List<Exporter> exporterList;
    private final ExecutorService executorService;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private TraceGenerator(final List<Exporter> exporterList) {
        this(exporterList, NUMBER_OF_THREADS);
    }

    private TraceGenerator(final List<Exporter> exporterList, final int numberOfThreads) {
        this.executorService = Executors.newFixedThreadPool(numberOfThreads);
        this.exporterList = exporterList;
    }

    public static TraceGenerator create(final List<Exporter> exporterList) {
        return new TraceGenerator(exporterList);
    }

    public TraceGenerator withThreads(final int threads) {
        return new TraceGenerator(this.exporterList, threads);
    }

    public TraceGenerator addOperation(final Task task, final long intervalMs) {
        executorService.submit(() -> {
            while (!isClosed.get()) {
                TraceBuilder.newTrace(task, exporterList.toArray(Exporter[]::new)).build();
                Try.run(() -> TimeUnit.MILLISECONDS.sleep(intervalMs));
            }
        });
        return this;
    }

    @Override
    public void close() throws IOException {
        isClosed.set(true);
    }
}
