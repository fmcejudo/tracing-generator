package com.github.fmcejudo.tracing.generator.exporter;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class CounterExporterTest {

    @SneakyThrows
    @Test
    void shouldCountWithMultiThreading() {

        //Given
        CounterExporter exporter = new CounterExporter();
        ExecutorService executorService = Executors.newFixedThreadPool(6);

        for (int i = 0; i < 20; i++) {
            executorService.submit(() -> {
                exporter.write("content".getBytes());
            });
        }
        //When
        executorService.awaitTermination(2, TimeUnit.SECONDS);

        //Then
        Assertions.assertThat(exporter.getCounterValue()).isEqualTo(20);
    }

}