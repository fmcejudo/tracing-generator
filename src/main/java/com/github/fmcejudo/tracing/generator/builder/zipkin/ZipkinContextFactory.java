package com.github.fmcejudo.tracing.generator.builder.zipkin;

import com.github.fmcejudo.tracing.generator.builder.OperationContext;
import com.github.fmcejudo.tracing.generator.builder.SpanClock;
import com.github.fmcejudo.tracing.generator.task.Task;
import io.vavr.API;
import io.vavr.Predicates;

import static io.vavr.API.$;
import static io.vavr.API.Case;

public final class ZipkinContextFactory {

    /**
     * It creates an object which will build the spans in a zipkin V2_JSON format
     *
     * @param task     as one instrumented task in a component
     * @param zipkinContext with parent information to build next
     * @return
     */
    public static OperationContext createOperationCtx(final Task task,
                                                      final SpanClock spanClock,
                                                      final ZipkinContext zipkinContext) {

        return API.Match(task.getComponent().getLocalComponent()).of(
                Case($(Predicates.isNull()), () -> {
                    throw new RuntimeException("local component can not be null");
                }),
                Case($("http"), HttpOperationContext.create(task, spanClock, zipkinContext)),
                Case($("jdbc"), JdbcOperationContext.create(task, spanClock)),
                Case($(), () -> {
                    throw new RuntimeException("local component does not exist or not implemented yet");
                }));
    }
}
