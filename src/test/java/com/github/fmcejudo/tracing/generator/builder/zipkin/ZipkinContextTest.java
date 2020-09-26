package com.github.fmcejudo.tracing.generator.builder.zipkin;

import com.github.fmcejudo.tracing.generator.builder.IdGenerator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ZipkinContextTest {

    @Test
    void shouldBuildAZipkinContext() {
        //Given
        long startTime = System.currentTimeMillis();
        String traceId = IdGenerator.generateId(128);
        //  When
        ZipkinContext zipkinContext = ZipkinContext.builder()
                .traceId(traceId)
                .startTime(startTime)
                .build();
        //Then
        Assertions.assertThat(zipkinContext).extracting("traceId", "parentId", "spanId", "startTime")
                .containsExactly(traceId, null, null, startTime);
    }


    @Test
    void shouldNotBuildAZipkinContextAsTraceId() {
        //Given && When && Then
        Assertions.assertThatThrownBy(() ->
                ZipkinContext.builder().startTime(System.currentTimeMillis()).build()
        ).isInstanceOf(NullPointerException.class).hasMessageContaining("traceId is marked non-null but is null");
    }

    @Test
    void shouldNotBuildAZipkinContextAsStartTime() {
        //Given && When && Then
        Assertions.assertThatThrownBy(() ->
                ZipkinContext.builder().traceId(IdGenerator.generateId(128)).build()
        ).isInstanceOf(NullPointerException.class).hasMessageContaining("startTime is marked non-null but is null");
    }


}