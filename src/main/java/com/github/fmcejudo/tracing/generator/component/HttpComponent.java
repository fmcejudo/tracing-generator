package com.github.fmcejudo.tracing.generator.component;

import com.github.fmcejudo.tracing.generator.issues.DurationDelayer;
import com.github.fmcejudo.tracing.generator.issues.HttpErrorIssuer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpComponent implements Component, DurationDelayer {

    private static final List<String> OPERATION_METHODS = List.of("get", "post", "put", "delete", "patch");

    private final String serviceName;
    private DurationDelayer durationDelayer;
    private HttpErrorIssuer httpErrorIssuer;

    public HttpComponent(final String serviceName) {
        this.serviceName = serviceName;
        this.durationDelayer = duration -> 0;
        httpErrorIssuer = HttpErrorIssuer.with(0, 0);
    }

    @Override
    public boolean hasKind() {
        return true;
    }

    @Override
    public Map<String, String> getServerTags(final String operation) {

        String[] operationChunks = operation.split(" ");
        String method = operationChunks[0];
        if (!OPERATION_METHODS.contains(method.toLowerCase())) {
            return Map.of();
        }

        Map<String, String> tags = new HashMap<>();
        tags.put("http.method", method);
        if (operationChunks.length > 1) {
            tags.put("http.path", operationChunks[1]);
            tags.put("http.url", "http://" + getServiceName() + "/" + operationChunks[1]);
        }

        httpErrorIssuer.randomIssuer()
                .map(HttpErrorIssuer.ErrorIssuer::getErrorTags)
                .ifPresent(tags::putAll);

        return Map.copyOf(tags);
    }

    @Override
    public Map<String, String> getClientTags(final Component childComponent, final String operationName) {
        Map<String, String> tags = new HashMap<>(childComponent.getServerTags(operationName));
        return Map.copyOf(tags);
    }

    @Override
    public String getLocalComponent() {
        return "http";
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public void configureExtraDurationFn(final DurationDelayer durationDelayer) {
        this.durationDelayer = durationDelayer;
    }

    @Override
    public long getExtraDuration(long duration) {
        return durationDelayer.getExtraDuration(duration);
    }

    public HttpComponent withFailurePercentage(final int failurePercentage) {
        this.httpErrorIssuer = HttpErrorIssuer.with(failurePercentage, httpErrorIssuer.warningPercentage());
        return this;
    }

    public HttpComponent withWarningPercentage(final int warningPercentage) {
        this.httpErrorIssuer = HttpErrorIssuer.with(httpErrorIssuer.failurePercentage(), warningPercentage);
        return this;
    }

    public int getFailurePercentage() {
        return httpErrorIssuer.failurePercentage();
    }

    public int getWarningPercentage() {
        return httpErrorIssuer.warningPercentage();
    }
}
