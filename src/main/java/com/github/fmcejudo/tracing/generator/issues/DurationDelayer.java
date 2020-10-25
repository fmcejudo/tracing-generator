package com.github.fmcejudo.tracing.generator.issues;

@FunctionalInterface
public interface DurationDelayer {

    long getExtraDuration(long duration);

    default void configureExtraDurationFn(DurationDelayer durationDelayer) {
    }
}
