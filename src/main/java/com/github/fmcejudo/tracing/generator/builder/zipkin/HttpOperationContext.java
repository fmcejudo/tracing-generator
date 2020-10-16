package com.github.fmcejudo.tracing.generator.builder.zipkin;

import com.github.fmcejudo.tracing.generator.builder.OperationContext;
import com.github.fmcejudo.tracing.generator.task.Task;
import zipkin2.Span;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.fmcejudo.tracing.generator.builder.IdGenerator.generateId;

final class HttpOperationContext extends AbstractOperationContext implements OperationContext {

    private final SpanContext spanContext;
    private final List<SpanContext> clientContextList = new ArrayList<>();
    private final String traceId;
    private final String serviceName;

    HttpOperationContext(final Task task, final ZipkinContext zipkinContext) {
        this.traceId = zipkinContext.getTraceId();
        this.serviceName = task.getServiceName();
        this.spanContext = SpanContext.builder()
                .parentId(zipkinContext.getParentId())
                .receiveTime(zipkinContext.getStartTime())
                .kind(Span.Kind.SERVER)
                .traceId(traceId)
                .name(task.getName())
                .tags(task.getTags())
                .serviceName(serviceName)
                .spanId(zipkinContext.getSpanId()).build();
    }

    static HttpOperationContext create(final Task task, final ZipkinContext zipkinContext) {
        return new HttpOperationContext(task, zipkinContext);
    }

    @Override
    public String addClient(final Task task, final long startTime) {

        Map<String, String> tags = task.getTags(this.spanContext.span());

        SpanContext spanClientContext = SpanContext.builder()
                .receiveTime(startTime)
                .traceId(this.traceId)
                .spanId(generateId(64))
                .name(task.getName())
                .kind(Span.Kind.CLIENT)
                .tags(this.spanContext.getTags())
                .parentId(this.spanContext.getSpanId())
                .serviceName(serviceName)
                .remoteServiceName(task.serviceName())
                .build();

        clientContextList.add(spanClientContext);
        return spanClientContext.getSpanId();
    }

    @Override
    public boolean isLeaf() {
        return clientContextList.isEmpty();
    }

    List<Span> generatedSpans() {
        List<Span> spanList = new ArrayList<>();
        spanList.add(this.spanContext.span());
        clientContextList.stream().map(SpanContext::span).forEach(spanList::add);
        return spanList;
    }

    @Override
    public String getSpanServerId() {
        return spanContext.getParentId();
    }

    @Override
    public boolean updateClientWithSpanId(final long responseTime, final String parentId) {
        Optional<SpanContext> span =
                clientContextList.stream().filter(s -> s.getSpanId().equals(parentId)).findFirst();
        span.ifPresent(s -> s.setResponseTime(responseTime));
        return span.isPresent();
    }

    @Override
    public void updateServerResponse(final long endTime) {
        spanContext.setResponseTime(endTime);
    }

    @Override
    public List<String> spanIdsInContext() {
        return Stream.concat(
                Stream.of(spanContext.getSpanId()),
                clientContextList.stream().map(SpanContext::getSpanId)
        ).collect(Collectors.toList());
    }

    public long duration() {
        return (spanContext.getResponseTime() - spanContext.getReceiveTime())/1_000;
    }

}

