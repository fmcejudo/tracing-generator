package com.github.fmcejudo.tracing.generator.builder;

import com.github.fmcejudo.tracing.generator.task.Task;

import java.util.Map;

public interface OperationContext {

    String addClient(Task task, long startTime);

    void updateServerResponse(long endTime);

    boolean updateClientWithSpanId(long responseTime, String parentId);

    byte[] message();

    long duration();

    boolean hasError();

    String getRemoteServerName();

    Map<String, String> getRemoteServerTags();

}
