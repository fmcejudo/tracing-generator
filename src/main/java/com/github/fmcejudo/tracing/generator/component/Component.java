package com.github.fmcejudo.tracing.generator.component;

import com.github.fmcejudo.tracing.generator.issues.DurationDelayer;

import java.util.Map;

public interface Component {

    boolean hasKind();

    Map<String, String> getServerTags(final String operationName);

    Map<String, String> getClientTags(Component childComponent, String operation);

    String getLocalComponent();

    String getServiceName();

}
