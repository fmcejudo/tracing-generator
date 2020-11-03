package com.github.fmcejudo.tracing.generator.builder.zipkin;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class ZipkinContext {

    @NonNull
    private final String traceId;
    private final String parentId;
    private final String spanId;

}
