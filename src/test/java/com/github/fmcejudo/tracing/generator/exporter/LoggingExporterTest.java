package com.github.fmcejudo.tracing.generator.exporter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

class LoggingExporterTest {

    @Test
    void shouldLogSpans() {
        //Given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        LoggingExporter loggingExporter = new LoggingExporter(outputStream);

        //When
        loggingExporter.write("content".getBytes());

        //Then
        Assertions.assertThat(outputStream.toString()).contains("content");
    }

}