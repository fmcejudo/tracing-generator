package com.github.fmcejudo.tracing.generator.exporter;

public interface Exporter {

    void write(final byte[] message);
}
