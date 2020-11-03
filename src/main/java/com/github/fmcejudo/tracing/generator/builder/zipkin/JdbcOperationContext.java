package com.github.fmcejudo.tracing.generator.builder.zipkin;

import com.github.fmcejudo.tracing.generator.builder.OperationContext;
import com.github.fmcejudo.tracing.generator.builder.SpanClock;
import com.github.fmcejudo.tracing.generator.task.Task;
import zipkin2.Span;

import java.util.List;
import java.util.Map;

public class JdbcOperationContext extends AbstractOperationContext {

    private final String serviceName;

    private final Map<String, String> databaseTags;

    private final long duration;

    private JdbcOperationContext(final Task task, final SpanClock spanClock) {
        super(spanClock);
        this.serviceName = task.serviceName();
        this.databaseTags = task.getServerTags();
        this.duration = task.getDuration();
    }

    public static OperationContext create(final Task task, final SpanClock spanClock) {
        return new JdbcOperationContext(task, spanClock);
    }

    @Override
    public String addClientForTask(final Task task) {
        throw new RuntimeException("Not Supported Operation");
    }

    @Override
    List<Span> generatedSpans() {
        return List.of();
    }

    @Override
    public boolean closeClientWithId(String spanId) {
        throw new RuntimeException("Not Supported Operation");
    }

    @Override
    public boolean hasError() {
        return false;
    }

    @Override
    public Map<String, String> getRemoteServerTags() {
        return databaseTags;
    }

    @Override
    public void closeOperation() {
    }

    public long operationDuration() {
        return duration;
    }
}
