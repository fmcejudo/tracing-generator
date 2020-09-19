package com.github.fmcejudo.tracing.generator.util;

import com.github.fmcejudo.tracing.generator.exporter.Exporter;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import zipkin2.Span;
import zipkin2.SpanBytesDecoderDetector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestTraceExporter implements Exporter {

    private List<List<Span>> spanList;
    private TraceAssert traceAssert;

    public TestTraceExporter() {
        spanList = new ArrayList<>();
        traceAssert = new TraceAssert(spanList);
    }

    @Override
    public void write(byte[] message) {
        List<Span> spans = SpanBytesDecoderDetector.decoderForListMessage(message).decodeList(message);
        spanList.add(spans);
    }

    public TraceAssert assertThat() {
        return traceAssert;
    }

    public static class TraceAssert extends AbstractAssert<TraceAssert, List<List<Span>>> {

        private TraceAssert(List<List<Span>> spans) {
            super(spans, TraceAssert.class);
        }

        public TraceAssert hasSize(int size) {
            if (this.actual.size() != size) {
                failWithMessage("Expected %d traces, but found %d", size, this.actual.size());
            }
            return this;
        }

        public TraceAssert hasSameTraceId() {
            if (this.actual.stream().flatMap(Collection::stream).map(Span::traceId).distinct().count() != 1) {
                failWithMessage("Expected all spans to have the same traceId, but they didn't");
            }
            return this;
        }

        public TraceAssert spanContainsTagKeys(final String... keys) {
            this.actual.stream().flatMap(Collection::stream)
                    .forEach(s -> Assertions.assertThat(s.tags()).containsKeys(keys));
            return this;
        }

        public TraceAssert spansHaveTimestamp() {
            if (this.actual.stream().flatMap(Collection::stream)
                    .anyMatch(s -> s.timestamp() == null)) {
                failWithMessage("Timestamp is mandatory field in span");
            }
            return this;
        }
    }

}
