package com.github.fmcejudo.tracing.generator.component;

import zipkin2.Span;

import java.util.Map;

public interface Component {

    String getLocalComponent();

    boolean hasKind();

    Map<String, String> getTags();

    Map<String, String> getTags(final Span span);
}
