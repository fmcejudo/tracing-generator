package com.github.fmcejudo.tracing.generator.issues;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public final class SimpleDurationDelayer implements DurationDelayer {

    private final AtomicInteger requestCounter;
    private final AtomicInteger requestWithLag;
    private final Random random;

    private int percentageOfRequest;
    private Function<Long, Long> incrementDurationFn;

    public SimpleDurationDelayer() {
        this.requestCounter = new AtomicInteger(0);
        this.requestWithLag = new AtomicInteger(0);
        this.random = new Random();
        percentageOfRequest = 0;
        incrementDurationFn = duration -> duration;
    }

    public SimpleDurationDelayer setPercentageOfRequests(final int percentageOfRequest) {
        this.percentageOfRequest = percentageOfRequest;
        return this;
    }

    public SimpleDurationDelayer incrementDurationFn(final Function<Long, Long> incrementDurationFn) {
        this.incrementDurationFn = incrementDurationFn;
        return this;
    }

    @Override
    public long getExtraDuration(long duration) {

        long incrementalDuration = 0;
        if (hasDurationLag()) {
            incrementalDuration = incrementDurationFn.apply(duration);
        }

        if (requestCounter.get() + requestWithLag.get() >= 100) {
            requestWithLag.set(0);
            requestCounter.set(0);
        }

        return incrementalDuration;
    }

    private boolean hasDurationLag() {

        //If number of request with lags has no reach maximum of available
        int randomPercentage = Double.valueOf(random.nextDouble() * 100).intValue();
        if (requestWithLag.get() < percentageOfRequest && randomPercentage <= percentageOfRequest) {
            requestWithLag.incrementAndGet();
            return true;
        }

        //If number of normal request has reach maximum normal and remaining request must be delayed
        if (100 - requestCounter.get() <= percentageOfRequest) {
            requestWithLag.incrementAndGet();
            return true;
        }
        requestCounter.incrementAndGet();
        return false;
    }
}
