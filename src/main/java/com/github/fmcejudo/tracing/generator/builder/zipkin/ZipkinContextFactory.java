package com.github.fmcejudo.tracing.generator.builder.zipkin;

import com.github.fmcejudo.tracing.generator.builder.OperationContext;
import com.github.fmcejudo.tracing.generator.operation.Operation;
import io.vavr.API;
import io.vavr.Predicates;

import static io.vavr.API.$;
import static io.vavr.API.Case;

public final class ZipkinContextFactory {

    /**
     * It creates an object which will build the spans in a zipkin V2_JSON format
     *
     * @param operation one of the available
     * @param zipkinContext with parent information to build next
     * @return
     */
    public static OperationContext createOperationCtx(final Operation operation,
                                                      final ZipkinContext zipkinContext) {

        return API.Match(operation.getComponent().getLocalComponent()).of(
                Case($(Predicates.isNull()), () -> {
                    throw new RuntimeException("local component can not be null");
                }),
                Case($("http"), HttpOperationContext.create(operation, zipkinContext)),
                Case($(), () -> {
                    throw new RuntimeException("local component does not exist or not implemented yet");
                }));
    }
}
