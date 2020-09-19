package com.github.fmcejudo.tracing.generator.service;

import com.github.fmcejudo.tracing.generator.builder.TraceBuilder;
import com.github.fmcejudo.tracing.generator.exporter.Exporter;
import com.github.fmcejudo.tracing.generator.operation.Operation;
import io.vavr.control.Try;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TraceGenerator {

    private static final int NUMBER_OF_THREADS = 3;

    private final List<Exporter> exporterList;
    private final ExecutorService executorService;

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

    public TraceGenerator addOperation(final Operation operation) {
        executorService.submit(() -> {
            while (true) {
                TraceBuilder.newTrace(operation).export(exporterList.toArray(Exporter[]::new));
                Try.run(() -> TimeUnit.MILLISECONDS.sleep(50L));
            }
        });
        return this;
    }
}
