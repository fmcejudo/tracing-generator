package com.github.fmcejudo.tracing.generator.builder;

import com.github.fmcejudo.tracing.generator.builder.zipkin.ZipkinContextFactory;
import com.github.fmcejudo.tracing.generator.exporter.Exporter;
import com.github.fmcejudo.tracing.generator.operation.Operation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Stream;

import static com.github.fmcejudo.tracing.generator.builder.IdGenerator.generateId;


public class TraceBuilder {

    private final Deque<OperationContext> operationQueue;
    private final String traceId;

    private final SpanClock spanClock;

    private TraceBuilder(final Operation rootOperation) {
        traceId = generateId(128);
        spanClock = new SpanClock();
        operationQueue = createSpansStack(rootOperation, null);
    }

    public static TraceBuilder newTrace(final Operation operation) {
        return new TraceBuilder(operation);
    }

    private Deque<OperationContext> createSpansStack(final Operation rootOperation, final String parentId) {
        Deque<OperationContext> spansStack = new ArrayDeque<>();
        OperationContext rootOperationContext = ZipkinContextFactory.createOperationCtx(
                rootOperation, traceId,  parentId, spanClock.getCurrentTimeInMillis()
        );

        spansStack.push(rootOperationContext);
        spanClock.advanceClockByMillis(10L);
        for (Operation op : rootOperation.getChildOperations()) {
            String spanId = rootOperationContext.addClient(op, spanClock.getCurrentTimeInMillis());
            spanClock.advanceClockByMillis(10L);
            Deque<OperationContext> subStack = createSpansStack(op, spanId);
            while (!subStack.isEmpty()) {
                spansStack.push(subStack.pollLast());
            }
        }
        return spansStack;
    }

    public void export(final Exporter... exporters) {

        List<String> serverSpanId = new ArrayList<>();

        while (!operationQueue.isEmpty()) {
            OperationContext stackedElement = operationQueue.pop();

            if (stackedElement.isLeaf()) {
                stackedElement.updateServerResponse(spanClock.getCurrentTimeInMillis());
                Stream.of(exporters).forEach(e -> e.write(stackedElement.message()));
                serverSpanId.add(stackedElement.getServerParentId());
                spanClock.advanceClockByMillis(10L);
                continue;
            }

            spanClock.advanceClockByMillis(10L);
            if (!serverSpanId.isEmpty()) {
                serverSpanId
                        .removeIf(id -> stackedElement.updateClientWithParentId(spanClock.getCurrentTimeInMillis(), id));

                spanClock.advanceClockByMillis(10L);
                stackedElement.updateServerResponse(spanClock.getCurrentTimeInMillis());
                serverSpanId.clear();
            }

            serverSpanId.add(stackedElement.getServerParentId());

            Stream.of(exporters).forEach(e -> e.write(stackedElement.message()));
        }
    }


    private static class SpanClock {
        private long advanceTime = 0L;

        public void advanceClockByMillis(long milliseconds) {
            advanceTime += milliseconds;
        }

        public long getCurrentTimeInMillis() {
            return System.currentTimeMillis() + advanceTime;
        }
    }
}
