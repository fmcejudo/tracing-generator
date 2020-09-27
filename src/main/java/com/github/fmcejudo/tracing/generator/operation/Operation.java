package com.github.fmcejudo.tracing.generator.operation;

import com.github.fmcejudo.tracing.generator.component.Component;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import zipkin2.Span;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class Operation {

    private final Component component;
    private final String name;
    private long duration = 10L;

    private List<Operation> childOperations = new ArrayList<>();

    public static Operation from(final Component component, final String operationName) {
        return new Operation(component, operationName);
    }

    public Map<String, String> getTags() {
        return component.getTags();
    }

    public Map<String, String> getTags(final Span responseSpan) {
        return component.getTags(responseSpan);
    }

    public String serviceName() {
        return Optional.ofNullable(this.getServiceName()).map(String::toLowerCase).orElse("");
    }

    public void addChildOperation(final Operation operation) {
        childOperations.add(operation);
    }

    public String getServiceName() {
        return component.getServiceName();
    }


}
