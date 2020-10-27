package com.github.fmcejudo.tracing.generator.builder.zipkin;

import com.github.fmcejudo.tracing.generator.builder.OperationContext;
import com.github.fmcejudo.tracing.generator.task.Task;
import zipkin2.Span;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.fmcejudo.tracing.generator.builder.IdGenerator.generateId;

final class HttpOperationContext extends AbstractOperationContext implements OperationContext {

    private final SpanContext serverSpanContext;
    private final List<SpanContext> clientSpanContextList = new ArrayList<>();
    private final String traceId;
    private final Task task;

    HttpOperationContext(final Task task, final ZipkinContext zipkinContext) {
        this.traceId = zipkinContext.getTraceId();
        this.task = task;
        this.serverSpanContext = SpanContext.builder()
                .parentId(zipkinContext.getParentId())
                .receiveTime(zipkinContext.getStartTime())
                .kind(Span.Kind.SERVER)
                .traceId(traceId)
                .name(task.getName())
                .tags(task.getServerTags())
                .serviceName(task.serviceName())
                .spanId(zipkinContext.getSpanId()).build();
    }

    static HttpOperationContext create(final Task task, final ZipkinContext zipkinContext) {
        return new HttpOperationContext(task, zipkinContext);
    }

    @Override
    public String addClient(final Task childrenTask, final long startTime) {

        SpanContext spanClientContext = SpanContext.builder()
                .receiveTime(startTime)
                .traceId(this.traceId)
                .spanId(generateId(64))
                .name(clientSpanName(childrenTask.getName()))
                .kind(Span.Kind.CLIENT)
                .tags(task.getClientTags(childrenTask))
                .parentId(this.serverSpanContext.getSpanId())
                .serviceName(task.serviceName())
                .build();

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
    public void updateServerResponse(final long endTime) {
        serverSpanContext.setResponseTime(endTime);
    }

    @Override
    public boolean updateClientWithSpanId(final long responseTime, final String parentId) {
        Optional<SpanContext> span =
                clientSpanContextList.stream().filter(s -> s.getSpanId().equals(parentId)).findFirst();
        span.ifPresent(s -> s.setResponseTime(responseTime));
        return span.isPresent();
    }

    @Override
    public boolean hasError() {
        return serverSpanContext.getTags().containsKey("error");
    }

    public long duration() {
        return (serverSpanContext.getResponseTime() - serverSpanContext.getReceiveTime()) / 1_000;
    }
}
