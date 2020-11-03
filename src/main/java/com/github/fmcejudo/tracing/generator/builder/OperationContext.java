package com.github.fmcejudo.tracing.generator.builder;

import com.github.fmcejudo.tracing.generator.task.Task;

import java.util.Map;

public interface OperationContext {

    String addClientForTask(Task task);

    boolean closeClientWithId(String parentId);

    void closeOperation();

    byte[] message();

    long operationDuration();

    boolean hasError();

    String getRemoteServerName();

    Map<String, String> getRemoteServerTags();

}
