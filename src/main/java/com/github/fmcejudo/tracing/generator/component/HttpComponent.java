package com.github.fmcejudo.tracing.generator.component;

import zipkin2.Span;

import java.util.HashMap;
import java.util.Map;

public class HttpComponent implements Component {

    private final String serviceName;

    public HttpComponent(final String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public boolean hasKind() {
        return true;
    }

    @Override
    public Map<String, String> getTags() {
        return Map.of();
    }

    @Override
    public Map<String, String> getTags(final Span span) {
        Map<String, String> tags = new HashMap<>(this.getTags());
        tags.put("http.method", "get");
        tags.put("http.url", span.name());
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
