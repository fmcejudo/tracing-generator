package com.github.fmcejudo.tracing.generator.component;


import com.github.fmcejudo.tracing.generator.issues.DurationDelayer;

import java.util.HashMap;
import java.util.Map;

public class JdbcComponent implements Component, DurationDelayer {

    private final String serviceName;
    private DurationDelayer durationDelayer;

    public JdbcComponent(String serviceName) {
        this.serviceName = serviceName;
        this.durationDelayer = duration -> 0;
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
        return getServerTags(operationName);
    }

    @Override
    public String getLocalComponent() {
        return "jdbc";
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public void configureExtraDurationFn(DurationDelayer durationDelayer) {
        this.durationDelayer = durationDelayer;
    }

    @Override
    public long getExtraDuration(long duration) {
        return durationDelayer.getExtraDuration(duration);
    }

}
