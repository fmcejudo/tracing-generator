package com.github.fmcejudo.tracing.generator.builder;

import com.github.fmcejudo.tracing.generator.component.HttpComponent;
import com.github.fmcejudo.tracing.generator.component.JdbcComponent;
import com.github.fmcejudo.tracing.generator.operation.Operation;
import com.github.fmcejudo.tracing.generator.util.TestTraceExporter;
import org.junit.jupiter.api.Test;

class TraceBuilderTest {

    @Test
    void shouldCreateATrace() {

        //Given
        TestTraceExporter exporter = new TestTraceExporter();
        Operation internetOperation = Operation.from(new HttpComponent(), "get /a/list", "service A");
        Operation operationInAMicroservice = Operation.from(new HttpComponent(), "get /list", "service B");

        internetOperation.addChildOperation(operationInAMicroservice);

        Operation queryingATaskDatabase = Operation.from(new JdbcComponent(), "select", "database");
        operationInAMicroservice.addChildOperation(queryingATaskDatabase);

        Operation insertingATaskDatabase = Operation.from(new JdbcComponent(), "insert", "database");
        operationInAMicroservice.addChildOperation(insertingATaskDatabase);

        //When
        TraceBuilder.newTrace(internetOperation).export(exporter);

        //Then
        exporter.assertThat().hasSize(4).hasSameTraceId().spansHaveTimestamp();
    }

}