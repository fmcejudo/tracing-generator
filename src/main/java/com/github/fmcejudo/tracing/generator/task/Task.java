package com.github.fmcejudo.tracing.generator.task;

import com.github.fmcejudo.tracing.generator.component.Component;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import zipkin2.Span;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class Task {

    private final Component component;
    private final String name;
    private long duration = 1000L;

    private final List<Task> childTasks = new ArrayList<>();

    public static Task from(final Component component, final String operationName) {
        return new Task(component, operationName, 1000L);
    }

    public Map<String, String> getTags() {
        return component.getTags(name);
    }

    public Map<String, String> getTags(final Span responseSpan) {
        return component.getTags(responseSpan);
    }

    public String serviceName() {
        return Optional.ofNullable(this.getServiceName()).map(String::toLowerCase).orElse("");
    }

    public String getServiceName() {
        return component.getServiceName();
    }

    public Task needsFrom(final Component service, String operationName) {
        Task childTask = Task.from(service, operationName);
        childTasks.add(childTask);
        return childTask;
    }
}
