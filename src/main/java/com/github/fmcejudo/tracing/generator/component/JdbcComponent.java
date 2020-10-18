package com.github.fmcejudo.tracing.generator.component;


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
    public Map<String, String> getServerTags(final String operation) {
        Map<String, String> tags = new HashMap<>();
        tags.put("sql.query", operation);
        return Map.copyOf(tags);
    }

    @Override
    public Map<String, String> getClientTags(final Component childComponent, final String operationName) {
        return Map.of();
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
