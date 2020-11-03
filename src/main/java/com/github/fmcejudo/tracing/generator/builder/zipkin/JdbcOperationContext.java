package com.github.fmcejudo.tracing.generator.builder.zipkin;

import com.github.fmcejudo.tracing.generator.builder.OperationContext;
import com.github.fmcejudo.tracing.generator.task.Task;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import zipkin2.Span;

import java.util.List;
import java.util.Map;

@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JdbcOperationContext extends AbstractOperationContext {

    private final String serviceName;

    private final Map<String, String> databaseTags;

    private final long duration;

    public static OperationContext create(final Task task) {
        return JdbcOperationContext.builder()
                .serviceName(task.getName())
                .databaseTags(task.getServerTags())
                .duration(task.getDuration())
                .build();
    }

    @Override
    public String addClient(final Task task, final long startTime) {
        throw new RuntimeException("Not Supported Operation");
    }

    @Override
    List<Span> generatedSpans() {
        return List.of();
    }

    @Override
    public boolean updateClientWithSpanId(long responseTime, String parentId) {
        throw new RuntimeException("Not Supported Operation");
    }

    @Override
    public boolean hasError() {
        return false;
    }

    @Override
    public String getRemoteServerName() {
        return serviceName;
    }

    @Override
    public Map<String, String> getRemoteServerTags() {
        return databaseTags;
    }

    @Override
    public void updateServerResponse(long endTime) {
    }

    public long duration() {
        return duration;
    }

}
