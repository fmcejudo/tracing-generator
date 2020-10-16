package com.github.fmcejudo.tracing.generator.task;

import com.github.fmcejudo.tracing.generator.assertions.OperationAssertions;
import com.github.fmcejudo.tracing.generator.component.HttpComponent;
import com.github.fmcejudo.tracing.generator.component.JdbcComponent;
import org.junit.jupiter.api.Test;

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

}