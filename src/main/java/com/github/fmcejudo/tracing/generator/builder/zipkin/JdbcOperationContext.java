package com.github.fmcejudo.tracing.generator.builder.zipkin;

import com.github.fmcejudo.tracing.generator.builder.OperationContext;
import com.github.fmcejudo.tracing.generator.task.Task;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import zipkin2.Endpoint;
import zipkin2.Span;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JdbcOperationContext extends AbstractOperationContext {

    private final Span.Builder clientSpanBuilder;
    private final long startTime;
    private final String spanId;

    public static OperationContext create(final Task task, final ZipkinContext zipkinContext) {
        Span.Builder spanBuilder = Span.newBuilder()
                .parentId(zipkinContext.getParentId())
                .id(zipkinContext.getSpanId())
                .traceId(zipkinContext.getTraceId())
                .name(task.getName())
                .kind(Span.Kind.CLIENT)
                .localEndpoint(Endpoint.newBuilder().serviceName(task.getServiceName()).build())
                .putTag("lc", "jdbc")
                .timestamp(zipkinContext.getStartTime())
                .duration(task.getDuration());

        return new JdbcOperationContext(spanBuilder, zipkinContext.getStartTime(), zipkinContext.getSpanId());
    }

    @Override
    public String getSpanServerId() {
        return clientSpanBuilder.build().parentId();
    }

    @Override
    public String addClient(final Task task, final long startTime) {
        throw new RuntimeException("Jdbc operations don't link with other component");
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    List<Span> generatedSpans() {
        return List.of(clientSpanBuilder.build());
    }

    @Override
    public boolean updateClientWithSpanId(long responseTime, String parentId) {
        throw new RuntimeException("Jdbc operation does not have children");
    }

    @Override
    public void updateServerResponse(long endTime) {
    }

    @Override
    public List<String> spanIdsInContext() {
        return List.of(spanId);
    }

    public long duration() {
        return clientSpanBuilder.build().durationAsLong() / 1_000_000;
    }

}
