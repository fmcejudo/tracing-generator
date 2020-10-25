package com.github.fmcejudo.tracing.generator.task;

import com.github.fmcejudo.tracing.generator.assertions.OperationAssertions;
import com.github.fmcejudo.tracing.generator.component.Component;
import com.github.fmcejudo.tracing.generator.component.HttpComponent;
import com.github.fmcejudo.tracing.generator.component.JdbcComponent;
import com.github.fmcejudo.tracing.generator.issues.DurationDelayer;
import com.github.fmcejudo.tracing.generator.issues.SimpleDurationDelayer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class TaskTest {

    @Test
    void shouldCreateAnAuthenticationSequence() {

        //Given && When
        HttpComponent service1 = new HttpComponent("service1");
        Task authenticationTask = Task.from(service1, "post /authentication");

        HttpComponent authenticationService = new HttpComponent("authenticationService");
        Task commonAuthenticationTask =
                authenticationTask.needsFrom(authenticationService, "post /authentication");

        JdbcComponent credentialsDatabase = new JdbcComponent("userCredentialDb");
        Task credentialRetrieverTask =
                commonAuthenticationTask.needsFrom(credentialsDatabase, "select from user-credentials");

        HttpComponent tokenValidationService = new HttpComponent("tokenValidation");
        Task tokenValidationTask = authenticationTask.needsFrom(tokenValidationService, "get /validate");

        //Then
        OperationAssertions.assertThat(authenticationTask)
                .dependsOnOperations(commonAuthenticationTask, tokenValidationTask)
                .componentInstanceOf(HttpComponent.class);


        OperationAssertions.assertThat(commonAuthenticationTask)
                .componentInstanceOf(HttpComponent.class)
                .dependsOnOperations(credentialRetrieverTask);

        OperationAssertions.assertThat(credentialRetrieverTask)
                .componentInstanceOf(JdbcComponent.class)
                .doesNotDependsOnAny();

        OperationAssertions.assertThat(tokenValidationTask)
                .componentInstanceOf(HttpComponent.class)
                .doesNotDependsOnAny();

    }

    @Test
    void shouldCreateTaskWithADurationDelayableComponent() {
        //Given
        CustomDelayableComponent customDelayableComponent = new CustomDelayableComponent() {{
            configureExtraDurationFn(new SimpleDurationDelayer()
                    .setPercentageOfRequests(100)
                    .incrementDurationFn(t -> 2 * t)
            );
        }};
        final long taskDuration = 5_000;
        Task task = new Task(customDelayableComponent, "get /operation").duration(taskDuration);

        //When
        long duration = task.getDuration();

        //Then
        Assertions.assertThat(duration).isEqualTo(taskDuration + 2 * taskDuration);
    }


    static class CustomDelayableComponent implements Component, DurationDelayer {

        private DurationDelayer durationDelayer = new SimpleDurationDelayer().incrementDurationFn(t -> t);

        @Override
        public boolean hasKind() {
            return false;
        }

        @Override
        public Map<String, String> getServerTags(String operationName) {
            return null;
        }

        @Override
        public Map<String, String> getClientTags(Component childComponent, String operation) {
            return null;
        }

        @Override
        public String getLocalComponent() {
            return null;
        }

        @Override
        public String getServiceName() {
            return null;
        }

        @Override
        public void configureExtraDurationFn(DurationDelayer durationDelayer) {
            this.durationDelayer = durationDelayer;
        }

        @Override
        public long getExtraDuration(long duration) {
            return durationDelayer.getExtraDuration(duration);
        }

    }

}