package com.github.fmcejudo.tracing.generator.builder.zipkin;


import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import zipkin2.Endpoint;
import zipkin2.Span;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
final class SpanContext {

    private String parentId;
    private String spanId;
    private String traceId;
    private String serviceName;
    private String remoteServiceName;
    private long receiveTime;
    private long responseTime;
    private String name;
    private Span.Kind kind;
    private String localComponent;
    @Builder.Default
    private Map<String, String> tags = new HashMap<>();

    Span span() {
        var spanBuilder = Span.newBuilder()
                .traceId(traceId)
                .parentId(parentId)
                .id(spanId)
                .name(name)
                .kind(kind)
                .timestamp(receiveTime)
                .duration(responseTime - receiveTime)
                .localEndpoint(Endpoint.newBuilder().serviceName(serviceName).build());

        if (StringUtils.isNotBlank(remoteServiceName)) {
            spanBuilder.remoteEndpoint(Endpoint.newBuilder().serviceName(remoteServiceName).build());
        }
        if (!tags.isEmpty()) {
            tags.forEach(spanBuilder::putTag);
        }
        return spanBuilder.build();
    }

}