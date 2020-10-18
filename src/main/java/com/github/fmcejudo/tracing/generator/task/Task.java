package com.github.fmcejudo.tracing.generator.task;

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
public class Task {

    private final Component component;
    private final String name;
    private long duration = 1000L;

    private final List<Task> childTasks = new ArrayList<>();

    public static Task from(final Component component, final String operationName) {
        return new Task(component, operationName, 1000L);
    }

    public Map<String, String> getServerTags() {
        return component.getServerTags(name);
    }

    public Map<String, String> getClientTags(final Task childTask) {
        return component.getClientTags(childTask.getComponent(), childTask.getName());
    }

    public String serviceName() {
        return Optional.ofNullable(component.getServiceName()).map(String::toLowerCase).orElse("");
    }

    public Task needsFrom(final Component service, String operationName) {
        Task childTask = Task.from(service, operationName);
        childTasks.add(childTask);
        return childTask;
    }
}
