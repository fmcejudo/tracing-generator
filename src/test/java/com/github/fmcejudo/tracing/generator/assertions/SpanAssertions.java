package com.github.fmcejudo.tracing.generator.assertions;

import org.assertj.core.api.AbstractAssert;
import org.junit.platform.commons.util.StringUtils;
import zipkin2.Span;

import java.util.Set;
import java.util.stream.Stream;

public class SpanAssertions extends AbstractAssert<SpanAssertions, Span> {

    private SpanAssertions(Span span) {
        super(span, SpanAssertions.class);
    }

    public static SpanAssertions assertThat(final Span span) {
        return new SpanAssertions(span);
    }

    public SpanAssertions hasTraceId(final String traceId) {
        if (traceId == null) {
            failWithMessage("traceId param can't be null");
        } else if (!traceId.equals(this.actual.traceId())) {
            failWithMessage("traceId %s does not match with %s", traceId, this.actual.traceId());
        }
        return this;
    }

    public SpanAssertions hasParentId(final String parentId) {
        if (parentId == null) {
            failWithMessage("parentId param can't be null");
        } else if (!parentId.equals(this.actual.parentId())) {
            failWithMessage("parentId %s does not match with %s", parentId, this.actual.parentId());
        }
        return this;
    }

    public SpanAssertions hasSpanId(final String spanId) {
        if (spanId == null) {
            failWithMessage("spanId param can't be null");
        } else if (!spanId.equals(this.actual.id())) {
            failWithMessage("spanId %s does not match with %s", spanId, this.actual.id());
        }
        return this;
    }

    public SpanAssertions hasKind(final String kind) {
        Span.Kind actualKind = this.actual.kind();
        if (actualKind == null) {
            failWithMessage("span with spanId %s has no kind", this.actual.id());
        }
        assert actualKind != null;
        if (!actualKind.name().equalsIgnoreCase(kind)) {
            failWithMessage(
                    "span with spanId %s has kind %s but expected %s", this.actual.id(), actualKind, kind.toUpperCase()
            );
        }
        return this;
    }

    public SpanAssertions hasName(final String name) {

        if (StringUtils.isBlank(name)) {
            failWithMessage("name can't be null");
        }

        String spanName = this.actual.name();
        if (StringUtils.isBlank(spanName) || !spanName.equalsIgnoreCase(name)) {
            failWithMessage("expected name %s, but found %s", name, spanName);
        }
        return this;
    }


    public SpanAssertions hasServiceName(final String serviceName) {

        if (StringUtils.isBlank(serviceName)) {
            failWithMessage("serviceName provided can't be null");
        }

        String localServiceName = this.actual.localServiceName();
        if (StringUtils.isBlank(localServiceName) || !localServiceName.equalsIgnoreCase(serviceName)) {
            failWithMessage("expected serviceName %s, but found %s", serviceName, localServiceName);
        }
        return this;
    }

    public SpanAssertions containsKeys(String... keys) {
        Set<String> keySet = this.actual.tags().keySet();
        if (!Stream.of(keys).allMatch(keySet::contains)) {
            failWithMessage("expected keys %s to be found in %s", String.join(",", keys), String.join(",", keySet));
        }
        return this;
    }

    public SpanAssertions doesNotContainKeys(String... keys) {
        Set<String> keySet = this.actual.tags().keySet();
        if (Stream.of(keys).anyMatch(keySet::contains)) {
            failWithMessage("expected keys %s not to be found in %s", String.join(",", keys), String.join(",", keySet));
        }
        return this;
    }
}
