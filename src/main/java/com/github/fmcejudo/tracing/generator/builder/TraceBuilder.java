package com.github.fmcejudo.tracing.generator.builder;

import com.github.fmcejudo.tracing.generator.builder.zipkin.ZipkinContext;
import com.github.fmcejudo.tracing.generator.builder.zipkin.ZipkinContextFactory;
import com.github.fmcejudo.tracing.generator.exporter.Exporter;
import com.github.fmcejudo.tracing.generator.task.Task;

import java.util.Iterator;
import java.util.List;

import static com.github.fmcejudo.tracing.generator.builder.IdGenerator.generateId;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;


public class TraceBuilder {

    private static final long LATENCY_MS = 5;
    private final String traceId;

    private final SimpleSpanClock simpleSpanClock;

    private final List<Exporter> exporterList;

    private final Task rootTask;

    private TraceBuilder(final Task rootTask, final Exporter... exporters) {
        this.rootTask = rootTask;
        traceId = generateId(128);
        simpleSpanClock = new SimpleSpanClock();
        exporterList = List.of(exporters);
    }

    public static TraceBuilder newTrace(final Task task, final Exporter... exporters) {
        return new TraceBuilder(task, exporters);
    }

    public void build() {
        build(rootTask, null);
    }

    private OperationContext build(final Task task, final String parentId) {

        ZipkinContext zipkinContext = ZipkinContext.builder()
                .parentId(parentId)
                .spanId(generateId(64))
                .traceId(traceId)
                .build();

        OperationContext operationContext =
                ZipkinContextFactory.createOperationCtx(task, simpleSpanClock, zipkinContext);

        if (operationContext.hasError()) {
            simpleSpanClock.advanceClockByMicroseconds(task.getDuration());
            operationContext.closeOperation();
            exportTrace(operationContext);
            return operationContext;
        }
        simpleSpanClock.advanceClockByMillis(LATENCY_MS);

        if (task.getChildTasks().size() != 0) {
            Iterator<Task> taskIterator = task.getChildTasks().iterator();
            while (taskIterator.hasNext()) {
                simpleSpanClock.advanceClockByMillis(LATENCY_MS);
                Task childrenTask = taskIterator.next();
                String clientSpanId =
                        operationContext.addClientForTask(childrenTask);
                simpleSpanClock.advanceClockByMillis(LATENCY_MS);
                OperationContext childrenOperation = build(childrenTask, clientSpanId);

                simpleSpanClock
                        .advanceClockByMillis(LATENCY_MS)
                        .advanceClockByMicroseconds(childrenOperation.operationDuration());

                operationContext.closeClientWithId(clientSpanId);
            }
        }
        simpleSpanClock
                .advanceClockByMillis(LATENCY_MS)
                .advanceClockByMicroseconds(task.getDuration());

        operationContext.closeOperation();
        exportTrace(operationContext);
        return operationContext;
    }

    private void exportTrace(final OperationContext operationContext) {
        exporterList.forEach(e -> e.write(operationContext.message()));
    }

    private static class SimpleSpanClock implements SpanClock {

        private long currentTime = System.currentTimeMillis();

        SimpleSpanClock advanceClockByMillis(long milliseconds) {
            currentTime += milliseconds;
            return this;
        }

        SimpleSpanClock advanceClockByMicroseconds(long microseconds) {
            advanceClockByMillis(MILLISECONDS.convert(microseconds, MICROSECONDS));
            return this;
        }

        public long getCurrentTimeInMicroseconds() {
            return MICROSECONDS.convert(currentTime, MILLISECONDS);
        }
    }

}

