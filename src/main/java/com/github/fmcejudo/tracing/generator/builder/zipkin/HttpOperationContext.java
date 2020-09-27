package com.github.fmcejudo.tracing.generator.builder.zipkin;

import com.github.fmcejudo.tracing.generator.builder.OperationContext;
import com.github.fmcejudo.tracing.generator.operation.Operation;
import zipkin2.Span;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.fmcejudo.tracing.generator.builder.IdGenerator.generateId;

final class HttpOperationContext extends AbstractOperationContext implements OperationContext {

    private final SpanContext spanContext;
    private final List<SpanContext> clientContextList = new ArrayList<>();
    private final String traceId;
    private final String serviceName;
    private final String name;

    HttpOperationContext(final Operation operation, final ZipkinContext zipkinContext) {
        this.traceId = zipkinContext.getTraceId();
        this.name = operation.getName();
        this.serviceName = operation.getServiceName();
        this.spanContext = SpanContext.builder()
                .parentId(zipkinContext.getParentId())
                .receiveTime(zipkinContext.getStartTime())
                .kind(Span.Kind.SERVER)
                .traceId(traceId)
                .name(name)
                .tags(operation.getTags())
                .serviceName(serviceName)
                .spanId(zipkinContext.getSpanId()).build();
    }

    static HttpOperationContext create(final Operation operation, final ZipkinContext zipkinContext) {
        return new HttpOperationContext(operation, zipkinContext);
    }

    @Override
    public String addClient(final Operation op, final long startTime) {

        Map<String, String> tags = op.getTags(this.spanContext.span());

        SpanContext spanClientContext = SpanContext.builder()
                .receiveTime(startTime)
                .traceId(this.traceId)
                .spanId(generateId(64))
                .name(op.getName())
                .kind(Span.Kind.CLIENT)
                .tags(tags)
                .parentId(this.spanContext.getSpanId())
                .serviceName(serviceName)
                .remoteServiceName(op.serviceName())
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
        return spanContext.getSpanId();
    }

    @Override
    public boolean updateClientWithParentId(final long responseTime, final String parentId) {
        Optional<SpanContext> span =
                clientContextList.stream().filter(s -> s.getSpanId().equals(parentId)).findFirst();
        span.ifPresent(s -> s.setResponseTime(responseTime));
        return span.isPresent();
    }

    @Override
    public void updateServerResponse(final long endTime) {
        spanContext.setResponseTime(endTime);
    }
}

