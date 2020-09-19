package com.github.fmcejudo.tracing.generator.exporter;

import java.io.OutputStream;
import java.io.PrintWriter;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LoggingExporter implements Exporter {

    private final PrintWriter writer;

    public LoggingExporter(final OutputStream outputStream) {
        this.writer = new PrintWriter(outputStream);
    }

    @Override
    public synchronized void write(byte[] message) {
        writer.write(new String(message, UTF_8));
        writer.write("\r\n");
        writer.flush();
    }
}
