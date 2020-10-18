package com.github.fmcejudo.tracing.generator.builder;

import com.github.fmcejudo.tracing.generator.component.HttpComponent;
import com.github.fmcejudo.tracing.generator.component.JdbcComponent;
import com.github.fmcejudo.tracing.generator.exporter.ZipkinExporter;
import com.github.fmcejudo.tracing.generator.task.Task;
import com.github.fmcejudo.tracing.generator.util.TestTraceExporter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class TraceBuilderTest {

    @Test
    void shouldCreateATrace() {

        //Given
        TestTraceExporter testTraceExporter = new TestTraceExporter();
        HttpComponent service1 = new HttpComponent("service1");
        Task authenticationTask = Task.from(service1, "post /authentication");

        HttpComponent authenticationService = new HttpComponent("authenticationService");
        Task commonAuthenticationTask =
                authenticationTask.needsFrom(authenticationService, "post /authentication");

        JdbcComponent credentialsDatabase = new JdbcComponent("userCredentialDb");
        commonAuthenticationTask.needsFrom(credentialsDatabase, "select from user-credentials");

        HttpComponent tokenValidationService = new HttpComponent("tokenValidation");
        authenticationTask.needsFrom(tokenValidationService, "get /validate");

        //When
        TraceBuilder.newTrace(authenticationTask, testTraceExporter, new ZipkinExporter("http://localhost:9411")).build();

        //Then
        testTraceExporter.assertThat().spansSize(4).hasSameTraceId().spansHaveTimestamp().spansHaveDuration();

        Assertions.assertThat(testTraceExporter.flatMapSpan()).hasSize(7);

    }

}