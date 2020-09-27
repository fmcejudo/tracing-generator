package com.github.fmcejudo.tracing.generator.component;


import zipkin2.Span;

import java.util.HashMap;
import java.util.Map;

public class JdbcComponent implements Component {

    private final String serviceName;

    public JdbcComponent(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public boolean hasKind() {
        return false;
    }

    @Override
    public Map<String, String> getTags(final String operation) {
        return Map.of();
    }

    @Override
    public Map<String, String> getTags(final Span span) {
        Map<String, String> tags = new HashMap<>(this.getTags(span.name()));
        tags.put("sql.query", "select * from tasks");
        return Map.copyOf(tags);
    }

    @Override
    public String getLocalComponent() {
        return "jdbc";
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }
}
