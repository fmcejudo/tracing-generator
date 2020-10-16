package com.github.fmcejudo.tracing.generator.builder;

import com.github.fmcejudo.tracing.generator.task.Task;

import java.util.List;

public interface OperationContext {

    String getSpanServerId();

    String addClient(Task task, long startTime);

    boolean isLeaf();

    byte[] message();

    long duration();

    boolean updateClientWithSpanId(long responseTime, String parentId);

    void updateServerResponse(long endTime);

    List<String> spanIdsInContext();
}
