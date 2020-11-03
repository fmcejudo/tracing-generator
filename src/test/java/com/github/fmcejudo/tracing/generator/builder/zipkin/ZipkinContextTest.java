package com.github.fmcejudo.tracing.generator.builder.zipkin;

import com.github.fmcejudo.tracing.generator.builder.IdGenerator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ZipkinContextTest {

    @Test
    void shouldBuildAZipkinContext() {
        //Given
        String traceId = IdGenerator.generateId(128);
        //  When
        ZipkinContext zipkinContext = ZipkinContext.builder()
                .traceId(traceId)
                .build();
        //Then
        Assertions.assertThat(zipkinContext).extracting("traceId", "parentId", "spanId")
                .containsExactly(traceId, null, null);
    }


    @Test
    void shouldNotBuildAZipkinContextAsTraceId() {
        //Given && When && Then
        Assertions.assertThatThrownBy(() ->
                ZipkinContext.builder().build()
        ).isInstanceOf(NullPointerException.class).hasMessageContaining("traceId is marked non-null but is null");
    }

}