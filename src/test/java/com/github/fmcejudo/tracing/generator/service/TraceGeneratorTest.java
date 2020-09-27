package com.github.fmcejudo.tracing.generator.service;

import com.github.fmcejudo.tracing.generator.component.HttpComponent;
import com.github.fmcejudo.tracing.generator.exporter.Exporter;
import com.github.fmcejudo.tracing.generator.exporter.LoggingExporter;
import com.github.fmcejudo.tracing.generator.operation.Operation;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;

class TraceGeneratorTest {


    @Mock
    private Exporter exporter;

    @BeforeEach
    void setUp() {
        exporter = Mockito.mock(LoggingExporter.class);
        Mockito.doNothing().when(exporter).write(any());
    }

    @SneakyThrows
    @Test
    void shouldGenerateSpansForOperation() {
        //Given
        Operation operation = Operation.from(new HttpComponent("service A"), "get /a");
        TraceGenerator.create(List.of(exporter))
                .withThreads(1)
                .addOperation(operation);

        //When
        TimeUnit.SECONDS.sleep(2);

        //Then
        Mockito.verify(exporter, Mockito.atLeast(5)).write(any());
    }

}