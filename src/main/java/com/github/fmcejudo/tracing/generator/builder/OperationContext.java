package com.github.fmcejudo.tracing.generator.builder;

import com.github.fmcejudo.tracing.generator.operation.Operation;

public interface OperationContext {

    String getSpanServerId();

    String addClient(Operation op, long startTime);

    boolean isLeaf();

    byte[] message();

    boolean updateClientWithParentId(long responseTime, String parentId);

    void updateServerResponse(long endTime);
}
