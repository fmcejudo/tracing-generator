package com.github.fmcejudo.tracing.generator.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcComponentTest {

    JdbcComponent jdbcComponent;

    @BeforeEach
    void setUp() {
        jdbcComponent = new JdbcComponent("database");
    }

    @Test
    void shouldValidateAJdbcComponent() {
        //Given && When && Then
        assertThat(jdbcComponent).extracting("serviceName", "localComponent").containsExactly("database", "jdbc");
    }

    @Test
    void shouldExtractServerTags() {
        //Given && When
        Map<String, String> serverTags = jdbcComponent.getServerTags("select * from table");
    }

}