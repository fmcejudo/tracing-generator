package com.github.fmcejudo.tracing.generator.builder.zipkin;

import com.github.fmcejudo.tracing.generator.builder.OperationContext;
import zipkin2.Span;
import zipkin2.codec.SpanBytesEncoder;

import java.util.List;

abstract class AbstractOperationContext implements OperationContext {

    @Override
    public byte[] message() {
        return SpanBytesEncoder.JSON_V2.encodeList(generatedSpans());
    }

    abstract List<Span> generatedSpans();
}
