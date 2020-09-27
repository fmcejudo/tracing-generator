package com.github.fmcejudo.tracing.generator.builder.zipkin;

import com.github.fmcejudo.tracing.generator.builder.OperationContext;
import com.github.fmcejudo.tracing.generator.operation.Operation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import zipkin2.Endpoint;
import zipkin2.Span;

import java.util.List;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JdbcOperationContext extends AbstractOperationContext {

    private final Span.Builder clientSpanBuilder;
    private final long startTime;
    private final String spanId;

    public static OperationContext create(final Operation operation, final ZipkinContext zipkinContext) {
        Span.Builder spanBuilder = Span.newBuilder()
                .parentId(zipkinContext.getParentId())
                .id(zipkinContext.getSpanId())
                .traceId(zipkinContext.getTraceId())
                .name(operation.getName())
                .kind(Span.Kind.CLIENT)
                .localEndpoint(Endpoint.newBuilder().serviceName(operation.getServiceName()).build())
                .putTag("lc", "jdbc")
                .timestamp(MICROSECONDS.convert(zipkinContext.getStartTime(), MILLISECONDS));

        return new JdbcOperationContext(spanBuilder, zipkinContext.getStartTime(), zipkinContext.getSpanId());
    }

    @Override
    public String getSpanServerId() {
        return spanId;
    }

    @Override
    public String addClient(final Operation op, final long startTime) {
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
    public boolean updateClientWithParentId(long responseTime, String parentId) {
        throw new RuntimeException("Jdbc operation does not have children");
    }

    @Override
    public void updateServerResponse(long endTime) {
        this.clientSpanBuilder.duration(MICROSECONDS.convert(endTime - startTime, MILLISECONDS));
    }
}
