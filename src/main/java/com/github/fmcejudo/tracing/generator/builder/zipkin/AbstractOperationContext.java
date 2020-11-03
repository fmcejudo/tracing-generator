package com.github.fmcejudo.tracing.generator.builder.zipkin;

import com.github.fmcejudo.tracing.generator.builder.OperationContext;
import com.github.fmcejudo.tracing.generator.builder.SpanClock;
import zipkin2.Span;
import zipkin2.codec.SpanBytesEncoder;

import java.util.List;

abstract class AbstractOperationContext implements OperationContext {

    private final SpanClock spanClock;

    AbstractOperationContext(final SpanClock spanClock) {
        this.spanClock = spanClock;
    }

    @Override
    public byte[] message() {
        return SpanBytesEncoder.JSON_V2.encodeList(generatedSpans());
    }

    abstract List<Span> generatedSpans();

    long getCurrentTimeInMicroseconds() {
        return spanClock.getCurrentTimeInMicroseconds();
    }
}
