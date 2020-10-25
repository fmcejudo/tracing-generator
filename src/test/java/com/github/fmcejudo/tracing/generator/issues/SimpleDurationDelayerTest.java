package com.github.fmcejudo.tracing.generator.issues;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class SimpleDurationDelayerTest {

    SimpleDurationDelayer simpleDurationDelayer;

    @BeforeEach
    void setUp() {
        simpleDurationDelayer = new SimpleDurationDelayer();
    }

    @Test
    @DisplayName("it includes to 5 percent of request an extra second")
    void shouldIncludeLagInSomeRequest() {

        //Given
        simpleDurationDelayer.setPercentageOfRequests(5).incrementDurationFn(l -> 1_000_000L);

        //When
        List<Long> durationOfRequestWithDelay = IntStream
                .range(0, 100)
                .boxed()
                .map(i -> simpleDurationDelayer.getExtraDuration(10))
                .filter(l -> l > 0).collect(Collectors.toList());

        //Then
        Assertions.assertThat(durationOfRequestWithDelay.size()).isEqualTo(5);
        durationOfRequestWithDelay
                .forEach(l -> Assertions.assertThat(l).isEqualTo(1_000_000L));
    }

}