package com.github.fmcejudo.tracing.generator.builder;

import com.github.fmcejudo.tracing.generator.component.HttpComponent;
import com.github.fmcejudo.tracing.generator.component.JdbcComponent;
import com.github.fmcejudo.tracing.generator.exporter.ZipkinExporter;
import com.github.fmcejudo.tracing.generator.task.Task;
import com.github.fmcejudo.tracing.generator.util.TestTraceExporter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import zipkin2.Span;

import java.util.List;
import java.util.stream.Collectors;

class TraceBuilderTest {

    TestTraceExporter testTraceExporter;

    ZipkinExporter zipkinExporter;

    @BeforeEach
    void setUp() {
        testTraceExporter = new TestTraceExporter();
        zipkinExporter = new ZipkinExporter("http://localhost:9411");
    }

    @Test
    void shouldCreateATrace() {

        //Given
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
        TraceBuilder.newTrace(authenticationTask, testTraceExporter).build();

        //Then
        testTraceExporter.assertThat().spansSize(6).hasSameTraceId().spansHaveTimestamp().spansHaveDuration();
    }


    @Test
    void shouldCreateAnIncompleteAndErrorTrace() {

        //Given
        HttpComponent migrogatewayComponent = new HttpComponent("microgateway");
        Task requestProductsTask = Task.from(migrogatewayComponent, "get /microgwt/product/list");

        HttpComponent productComponent = new HttpComponent("product");
        Task productRequestTask = requestProductsTask.needsFrom(productComponent, "get /product/list");

        HttpComponent catalogComponent = new HttpComponent("catalog").withFailurePercentage(100);
        productRequestTask.needsFrom(catalogComponent, "get /catalog/list");

        //When
        TraceBuilder.newTrace(requestProductsTask, testTraceExporter).build();

        //Then
        List<Span> spans = testTraceExporter.flatMapSpan();
        List<Span> spansWithError = spans.stream()
                .filter(s -> s.tags().containsKey("error"))
                .collect(Collectors.toList());
        Assertions.assertThat(spansWithError).hasSize(2); //Catalog-Server && Product-Client
    }

    @Test
    void shouldCreateAnIncompleteAndWarningTrace() {

        //Given
        HttpComponent migrogatewayComponent = new HttpComponent("microgateway");
        Task requestProductsTask = Task.from(migrogatewayComponent, "get /microgwt/purchase/list");

        HttpComponent purchaseComponent = new HttpComponent("purchase");
        Task productRequestTask = requestProductsTask.needsFrom(purchaseComponent, "get /purchase/list");

        HttpComponent catalogComponent = new HttpComponent("catalog").withWarningPercentage(100);
        productRequestTask.needsFrom(catalogComponent, "get /catalog/list");

        //When
        TraceBuilder.newTrace(requestProductsTask, testTraceExporter).build();

        //Then
        List<Span> spans = testTraceExporter.flatMapSpan();

        testTraceExporter.assertThat().spansSize(5)
                .hasSameTraceId()
                .spansHaveDuration()
                .spansHaveTimestamp();

        List<Span> spansWithWarning = spans.stream()
                .filter(s -> s.tags().containsKey("http.status_code"))
                .collect(Collectors.toList());

        Assertions.assertThat(spansWithWarning).hasSize(2); //Catalog-Server && Purchase-Client
    }

    @Test
    void shouldCreateANonFinishedTask() {

        //Given
        HttpComponent migrogatewayComponent = new HttpComponent("gateway");
        Task requestOrdersTask = Task.from(migrogatewayComponent, "get /microgwt/orders/list");

        HttpComponent purchaseComponent = new HttpComponent("purchase");
        Task productRequestTask = requestOrdersTask.needsFrom(purchaseComponent, "get /purchase/list");

        HttpComponent catalogComponent = new HttpComponent("catalog").withFailurePercentage(100);
        Task catalogTask = productRequestTask.needsFrom(catalogComponent, "get /catalog/list");

        HttpComponent userComponent = new HttpComponent("user");
        catalogTask.needsFrom(userComponent, "get /user/list");

        //When
        TraceBuilder.newTrace(requestOrdersTask, testTraceExporter).build();

        //Then
        List<Span> spans = testTraceExporter.flatMapSpan();

        testTraceExporter.assertThat().spansSize(5)
                .hasSameTraceId()
                .spansHaveDuration()
                .spansHaveTimestamp();

        List<Span> spansWithError = spans.stream()
                .filter(s -> s.tags().containsKey("error"))
                .collect(Collectors.toList());
        Assertions.assertThat(spansWithError).hasSize(2); //Catalog-Server && purchase-Client
    }

}