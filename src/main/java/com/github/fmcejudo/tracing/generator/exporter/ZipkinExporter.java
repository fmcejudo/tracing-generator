package com.github.fmcejudo.tracing.generator.exporter;

import lombok.extern.slf4j.Slf4j;
import zipkin2.Span;
import zipkin2.SpanBytesDecoderDetector;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;

@Slf4j
public class ZipkinExporter implements Exporter {

    private final AsyncReporter<Span> reporter;

    public ZipkinExporter(final String zipkinUrlBase) {

        this.reporter = AsyncReporter.builder(OkHttpSender.create(zipkinUrlBase + "/api/v2/spans"))
                .queuedMaxSpans(10)
                .build();
    }

    @Override
    public synchronized void write(byte[] message) {
        SpanBytesDecoderDetector.decoderForListMessage(message).decodeList(message)
                .forEach(reporter::report);
    }

}
