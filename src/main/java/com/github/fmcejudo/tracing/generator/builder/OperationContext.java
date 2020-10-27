package com.github.fmcejudo.tracing.generator.builder;

import com.github.fmcejudo.tracing.generator.task.Task;

public interface OperationContext {

    String addClient(Task task, long startTime);

    byte[] message();

    long duration();

    void updateServerResponse(long endTime);

    boolean updateClientWithSpanId(long responseTime, String parentId);

    boolean hasError();

}
