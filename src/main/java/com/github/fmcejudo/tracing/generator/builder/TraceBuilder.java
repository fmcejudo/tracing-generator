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

    private static final long LATENCY = 5;
    private final String traceId;

    private final SpanClock spanClock;

    private final List<Exporter> exporterList;

    private final Task rootTask;

    private TraceBuilder(final Task rootTask, final Exporter... exporters) {
        this.rootTask = rootTask;
        traceId = generateId(128);
        spanClock = new SpanClock();
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
                .startTime(spanClock.getCurrentTimeInMicroseconds())
                .build();

        OperationContext operationContext = ZipkinContextFactory.createOperationCtx(task, zipkinContext);

        spanClock.advanceClockByMillis(LATENCY);

        if (task.getChildTasks().size() != 0) {
            Iterator<Task> taskIterator = task.getChildTasks().iterator();
            while (taskIterator.hasNext()) {
                spanClock.advanceClockByMillis(LATENCY);
                Task childrenTask = taskIterator.next();
                String clientSpanId =
                        operationContext.addClient(childrenTask, spanClock.getCurrentTimeInMicroseconds());
                OperationContext childrenOperation = build(childrenTask, clientSpanId);
                spanClock.advanceClockByMillis(childrenOperation.duration() / 1000  + LATENCY);
                operationContext.updateClientWithSpanId(spanClock.getCurrentTimeInMicroseconds(), clientSpanId);
            }
        }
        spanClock.advanceClockByMillis(LATENCY + task.getDuration()/ 1000);
        operationContext.updateServerResponse(spanClock.getCurrentTimeInMicroseconds());
        exportTrace(operationContext);
        return operationContext;
    }

    private void exportTrace(final OperationContext operationContext) {
        exporterList.forEach(e -> e.write(operationContext.message()));
    }

    private static class SpanClock {
        private long currentTime = System.currentTimeMillis();

        public void advanceClockByMillis(long milliseconds) {
            currentTime += milliseconds;
        }

        public long getCurrentTimeInMicroseconds() {
            return MICROSECONDS.convert(currentTime, MILLISECONDS);
        }
    }
}
