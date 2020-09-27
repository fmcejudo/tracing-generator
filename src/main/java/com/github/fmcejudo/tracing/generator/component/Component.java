package com.github.fmcejudo.tracing.generator.component;

import zipkin2.Span;

import java.util.Map;

public interface Component {

    boolean hasKind();

    Map<String, String> getTags(final String operationName);

    Map<String, String> getTags(final Span span);

    String getLocalComponent();

    String getServiceName();
}
