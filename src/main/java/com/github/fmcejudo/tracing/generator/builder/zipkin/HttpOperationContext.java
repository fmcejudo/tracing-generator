package com.github.fmcejudo.tracing.generator.builder.zipkin;

import com.github.fmcejudo.tracing.generator.builder.OperationContext;
import com.github.fmcejudo.tracing.generator.builder.SpanClock;
import com.github.fmcejudo.tracing.generator.task.Task;
import zipkin2.Span;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.fmcejudo.tracing.generator.builder.IdGenerator.generateId;

final class HttpOperationContext extends AbstractOperationContext implements OperationContext {

    private final SpanContext serverSpanContext;
    private final List<SpanContext> clientSpanContextList = new ArrayList<>();
    private final String traceId;
    private final Task task;

    HttpOperationContext(final Task task, final SpanClock spanClock, final ZipkinContext zipkinContext) {
        super(spanClock);

        this.traceId = zipkinContext.getTraceId();
        this.task = task;
        this.serverSpanContext = SpanContext.builder()
                .parentId(zipkinContext.getParentId())
                .receiveTime(getCurrentTimeInMicroseconds())
                .kind(Span.Kind.SERVER)
                .traceId(traceId)
                .name(task.getName())
                .tags(task.getServerTags())
                .serviceName(task.serviceName())
                .spanId(zipkinContext.getSpanId()).build();
    }

    static HttpOperationContext create(final Task task, final SpanClock spanClock, final ZipkinContext zipkinContext) {
        return new HttpOperationContext(task, spanClock, zipkinContext);
    }

    @Override
    public String addClientForTask(final Task childrenTask) {

        SpanContext.SpanContextBuilder contextBuilder = SpanContext.builder()
                .receiveTime(getCurrentTimeInMicroseconds())
                .traceId(this.traceId)
                .spanId(generateId(64))
                .name(clientSpanName(childrenTask.getName()))
                .kind(Span.Kind.CLIENT)
                .tags(task.getClientTags(childrenTask))
                .parentId(this.serverSpanContext.getSpanId())
                .serviceName(task.serviceName());

        Optional.ofNullable(childrenTask.remoteServiceName()).ifPresent(contextBuilder::remoteServiceName);

        SpanContext spanClientContext = contextBuilder.build();

        clientSpanContextList.add(spanClientContext);
        return spanClientContext.getSpanId();
    }

    private String clientSpanName(final String operationName) {
        return Stream.of(operationName.split(" ")).findFirst().orElse("get");
    }

    List<Span> generatedSpans() {
        List<Span> spanList = new ArrayList<>();
        spanList.add(this.serverSpanContext.span());
        clientSpanContextList.stream().map(SpanContext::span).forEach(spanList::add);
        return spanList;
    }

    @Override
    public void closeOperation() {
        serverSpanContext.setResponseTime(getCurrentTimeInMicroseconds());
    }

    @Override
    public boolean closeClientWithId(final String spanId) {
        Optional<SpanContext> span =
                clientSpanContextList.stream().filter(s -> s.getSpanId().equals(spanId)).findFirst();
        span.ifPresent(s -> s.setResponseTime(getCurrentTimeInMicroseconds()));
        return span.isPresent();
    }

    @Override
    public boolean hasError() {
        return serverSpanContext.getTags().containsKey("error");
    }

    @Override
    public long operationDuration() {
        return (serverSpanContext.getResponseTime() - serverSpanContext.getReceiveTime()) / 1_000;
    }

    @Override
    public Map<String, String> getRemoteServerTags() {
        return serverSpanContext.getTags();
    }
}
