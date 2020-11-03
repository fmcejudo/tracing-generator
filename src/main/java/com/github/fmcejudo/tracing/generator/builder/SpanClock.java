package com.github.fmcejudo.tracing.generator.builder;

public interface SpanClock {

    long getCurrentTimeInMicroseconds();
}
