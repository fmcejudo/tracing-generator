package com.github.fmcejudo.tracing.generator.component;

import zipkin2.Span;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpComponent implements Component {

    private final String serviceName;
    private static final List<String> OPERATION_METHODS = List.of("get", "post", "put", "delete", "patch");


    public HttpComponent(final String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public boolean hasKind() {
        return true;
    }

    @Override
    public Map<String, String> getTags(final String operation) {

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
        return Map.copyOf(tags);
    }

    @Override
    public Map<String, String> getTags(final Span span) {
        Map<String, String> tags = new HashMap<>(this.getTags(span.name()));
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
}
