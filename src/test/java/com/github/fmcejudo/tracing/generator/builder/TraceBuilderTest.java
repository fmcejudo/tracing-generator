package com.github.fmcejudo.tracing.generator.builder;

import com.github.fmcejudo.tracing.generator.component.HttpComponent;
import com.github.fmcejudo.tracing.generator.operation.Operation;
import com.github.fmcejudo.tracing.generator.util.TestTraceExporter;
import org.junit.jupiter.api.Test;

class TraceBuilderTest {

    @Test
    void shouldCreateATrace() {

        //Given
        TestTraceExporter exporter = new TestTraceExporter();

        Operation internetOperation = Operation.from(new HttpComponent(), "get /a/list", "service A"

        );
        Operation operationInAMicroservice = Operation.from(new HttpComponent(), "get /list", "service B");

        internetOperation.addChildOperation(operationInAMicroservice);

        //When
        TraceBuilder.newTrace(internetOperation).export(exporter);

        //Then
        exporter.assertThat().hasSize(2).hasSameTraceId().spansHaveTimestamp();
    }

}