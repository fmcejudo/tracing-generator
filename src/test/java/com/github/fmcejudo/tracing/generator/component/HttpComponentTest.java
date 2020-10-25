package com.github.fmcejudo.tracing.generator.component;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HttpComponentTest {

    HttpComponent httpComponent;

    @BeforeEach
    void setUp() {
        httpComponent = new HttpComponent("my-application");
    }

    @Test
    void shouldDefineAHttpComponent() {
        //Given && When && Then
        assertThat(httpComponent)
                .extracting("localComponent", "serviceName")
                .containsExactly("http", "my-application");

        for (int i = 0; i < 100; i++) {
            assertThat(httpComponent.getExtraDuration(10)).isEqualTo(0);
        }
    }

    @Test
    void shouldExtractServerTags() {
        //Given && When
        Map<String, String> serverTags = httpComponent.getServerTags("get /operation");

        //Then
        assertThat(serverTags)
                .containsEntry("http.method", "get")
                .containsEntry("http.url", "http://my-application//operation")
                .containsEntry("http.path", "/operation");
    }

    @Test
    void shouldExtractClientTags() {
        //Given
        var childrenComponent = new JdbcComponent("database");

        //When
        Map<String, String> clientTags = httpComponent.getClientTags(childrenComponent, "select * from table");

        //Then
        assertThat(clientTags).containsEntry("sql.query", "select * from table");
    }

}