package com.github.fmcejudo.tracing.generator.component;

import zipkin2.Span;

import java.util.Map;

public class HttpComponent implements Component {

    @Override
    public String getLocalComponent() {
        return "http";
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
    public Map<String, String> getTags(Span span) {
        return this.getTags();
    }
}
