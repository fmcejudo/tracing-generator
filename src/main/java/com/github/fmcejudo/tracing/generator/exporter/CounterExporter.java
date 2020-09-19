package com.github.fmcejudo.tracing.generator.exporter;

import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class CounterExporter implements Exporter {

    private final AtomicInteger counter;

    {
        long startTime = System.currentTimeMillis();
        counter = new AtomicInteger(0);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                long seconds = SECONDS.convert(System.currentTimeMillis() - startTime, MILLISECONDS);
                System.out.println("counting " + counter.get() + " elements in " + seconds + " seconds");
            }
        });
    }

    @Override
    public void write(byte[] message) {
        counter.incrementAndGet();
    }

    int getCounterValue() {
        return counter.intValue();
    }


}
