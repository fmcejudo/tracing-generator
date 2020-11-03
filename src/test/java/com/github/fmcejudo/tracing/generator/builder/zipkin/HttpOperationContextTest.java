package com.github.fmcejudo.tracing.generator.builder.zipkin;

import com.github.fmcejudo.tracing.generator.assertions.SpanAssertions;
import com.github.fmcejudo.tracing.generator.builder.SpanClock;
import com.github.fmcejudo.tracing.generator.component.HttpComponent;
import com.github.fmcejudo.tracing.generator.task.Task;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import zipkin2.Span;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mockito.Mockito.when;

class HttpOperationContextTest {

    SpanClock spanClock;

    @BeforeEach
    void setUp() {
        AtomicLong currentTime = new AtomicLong(System.currentTimeMillis());
        this.spanClock = Mockito.mock(SpanClock.class);
        when(spanClock.getCurrentTimeInMicroseconds())
                .then(a -> {
                    currentTime.set(currentTime.get() + 10);
                    return MICROSECONDS.convert(currentTime.get(), MILLISECONDS);
                });
    }

    @Test
    void shouldCreateSpansWithoutErrors() {

        //Given
        HttpComponent httpComponent = new HttpComponent("custom-service");
        Task task = Task.from(httpComponent, "get /actuator/health");

        HttpOperationContext operationContext = HttpOperationContext.create(task, spanClock,
                ZipkinContext.builder()
                        .traceId("00000001")
                        .parentId("00000002")
                        .spanId("00000003")
                        .build()
        );

        operationContext.closeOperation();

        //When
        List<Span> spans = operationContext.generatedSpans();

        //Then
        Assertions.assertThat(spans).hasSize(1);
        SpanAssertions.assertThat(spans.get(0))
                .hasTraceId("0000000000000001")
                .hasParentId("0000000000000002")
                .hasSpanId("0000000000000003")
                .hasKind("SERVER")
                .hasName("get /actuator/health")
                .hasServiceName("custom-service")
                .containsKeys("http.url", "http.path")
                .doesNotContainKeys("error", "http.status_code");

    }

    @Test
    void shouldCreateSpansWithErrors() {


        //Given
        HttpComponent httpComponent = new HttpComponent("custom-service").withFailurePercentage(100);
        Task task = Task.from(httpComponent, "get /error");

        HttpOperationContext operationContext = HttpOperationContext.create(task, spanClock,
                ZipkinContext.builder()
                        .traceId("00000004")
                        .parentId("00000005")
                        .spanId("00000006")
                        .build()
        );
        operationContext.closeOperation();

        //When
        List<Span> spans = operationContext.generatedSpans();

        //Then
        Assertions.assertThat(spans).hasSize(1);
        SpanAssertions.assertThat(spans.get(0))
                .hasTraceId("0000000000000004")
                .hasParentId("0000000000000005")
                .hasSpanId("0000000000000006")
                .hasKind("SERVER")
                .hasName("get /error")
                .hasServiceName("custom-service")
                .containsKeys("http.url", "http.path", "error")
                .doesNotContainKeys("http.status_code");
    }

    @Test
    void shouldCreateSpansWithWarnings() {

        //Given
        HttpComponent httpComponent = new HttpComponent("custom-service").withWarningPercentage(100);
        Task task = Task.from(httpComponent, "get /warning");

        HttpOperationContext operationContext = HttpOperationContext.create(task, spanClock,
                ZipkinContext.builder()
                        .traceId("00000007")
                        .parentId("00000008")
                        .spanId("00000009")
                        .build()
        );
        operationContext.closeOperation();

        //When
        List<Span> spans = operationContext.generatedSpans();

        //Then
        Assertions.assertThat(spans).hasSize(1);
        SpanAssertions.assertThat(spans.get(0))
                .hasTraceId("0000000000000007")
                .hasParentId("0000000000000008")
                .hasSpanId("0000000000000009")
                .hasKind("SERVER")
                .hasName("get /warning")
                .hasServiceName("custom-service")
                .containsKeys("http.url", "http.path", "http.status_code")
                .doesNotContainKeys("error");
    }

}