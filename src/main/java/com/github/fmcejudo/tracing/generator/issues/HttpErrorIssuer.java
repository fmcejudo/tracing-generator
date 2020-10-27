package com.github.fmcejudo.tracing.generator.issues;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpErrorIssuer {

    private final AtomicInteger requestCounter;

    private final FailureIssuer failureIssuer;

    private final WarningIssuer warningIssuer;

    private HttpErrorIssuer(final int failurePercentage, final int warningPercentage) {
        this.failureIssuer = new FailureIssuer(failurePercentage);
        this.warningIssuer = new WarningIssuer(warningPercentage);
        this.requestCounter = new AtomicInteger(0);
    }

    public static HttpErrorIssuer with(final int failurePercentage, final int warningPercentage) {
        if (failurePercentage + warningPercentage > 100) {
            throw new RuntimeException("error percentage cannot go over hundred percent");
        }

        return new HttpErrorIssuer(failurePercentage, warningPercentage);
    }


    public synchronized Optional<ErrorIssuer> randomIssuer() {


        int requestNumber = requestCounter.intValue();

        if (requestNumber + warningIssuer.getCounter() + failureIssuer.getCounter() >= 100) {
            failureIssuer.resetCounter();
            warningIssuer.resetCounter();
            requestCounter.set(0);
        }

        if (failureIssuer.generateIssuer()) {
            return Optional.of(failureIssuer);
        }

        if (warningIssuer.generateIssuer()) {
            return Optional.of(warningIssuer);
        }

        //Generate normal request if it is possible based in the error percentage
        if (100 - requestNumber > failureIssuer.errorPercentage() + warningIssuer.errorPercentage()) {
            requestCounter.incrementAndGet();
            return Optional.empty();
        }

        //Generate remaining failures
        if (failureIssuer.getCounter() < failureIssuer.errorPercentage()) {
            failureIssuer.incrementCounter();
            return Optional.of(failureIssuer);
        }


        //Generate remaining warnings
        if (warningIssuer.getCounter() < warningIssuer.errorPercentage()) {
            warningIssuer.incrementCounter();
            return Optional.of(warningIssuer);
        }
        return Optional.empty();
    }

    public int failurePercentage() {
        return failureIssuer.errorPercentage();
    }

    public int warningPercentage() {
        return warningIssuer.errorPercentage();
    }

    public interface ErrorIssuer {

        int errorPercentage();

        Map<String, String> getErrorTags();
    }

    private static abstract class AbstractErrorIssuer implements ErrorIssuer {

        private final AtomicInteger errorCounter;

        private final int percentage;

        private final Random random;

        private AbstractErrorIssuer(final int percentage) {
            this.percentage = percentage;
            this.errorCounter = new AtomicInteger(0);
            this.random = new Random();
        }

        @Override
        public int errorPercentage() {
            return percentage;
        }

        boolean generateIssuer() {
            int randomPercentage = Double.valueOf(random.nextDouble() * 100).intValue();
            //If number of error request has no reached maximum
            if (errorCounter.intValue() < percentage && randomPercentage <= percentage) {
                errorCounter.incrementAndGet();
                return true;
            }
            return false;
        }

        void incrementCounter() {
            errorCounter.incrementAndGet();
        }

        void resetCounter() {
            errorCounter.set(0);
        }

        int getCounter() {
            return errorCounter.intValue();
        }

    }

    private static class FailureIssuer extends AbstractErrorIssuer {

        private FailureIssuer(int percentage) {
            super(percentage);
        }

        @Override
        public Map<String, String> getErrorTags() {
            return Map.of("error", "true");
        }
    }

    private static class WarningIssuer extends AbstractErrorIssuer {

        private WarningIssuer(int percentage) {
            super(percentage);
        }

        @Override
        public Map<String, String> getErrorTags() {
            return Map.of("http.status_code", "306");
        }
    }
}
