package com.github.fmcejudo.tracing.generator.issues;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class HttpErrorIssuerTest {


    @RepeatedTest(value = 20)
    void shouldGenerateHttpErrors() {
        //Given
        final int errorPercentage = 10;
        final int warningPercentage = 10;
        HttpErrorIssuer httpErrorIssuer = HttpErrorIssuer.with(errorPercentage, warningPercentage);

        //When
        List<HttpErrorIssuer.ErrorIssuer> nonEmptyErrorIssuer = IntStream.range(0, 100)
                .boxed()
                .map(i -> httpErrorIssuer.randomIssuer())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        //Then
        Assertions.assertThat(nonEmptyErrorIssuer).hasSize(warningPercentage + errorPercentage);
        long failureIssuer = nonEmptyErrorIssuer
                .stream()
                .filter(issuer -> issuer.getErrorTags().containsKey("error"))
                .count();

        Assertions.assertThat(failureIssuer).isEqualTo(errorPercentage);

        Assertions.assertThat(httpErrorIssuer.failurePercentage()).isEqualTo(errorPercentage);
        Assertions.assertThat(httpErrorIssuer.warningPercentage()).isEqualTo(warningPercentage);
    }

    @Test
    void shouldFailIfPercentageIsOverHundredPercent() {
        //Given && When && Then
        Assertions.assertThatThrownBy(() -> HttpErrorIssuer.with(70, 70))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("error percentage cannot go over hundred percent");
    }

    @Test
    void shouldGenerateOnlyFailureRequests() {
        //Given
        HttpErrorIssuer httpErrorIssuer = HttpErrorIssuer.with(100, 0);

        //When
        Optional<HttpErrorIssuer.ErrorIssuer> errorIssuer = httpErrorIssuer.randomIssuer();

        //Then
        Assertions.assertThat(errorIssuer).isPresent();
        Assertions.assertThat(errorIssuer.get().getErrorTags()).containsEntry("error", "true");
        Assertions.assertThat(errorIssuer.get().errorPercentage()).isEqualTo(100);

    }

    @Test
    void shouldGenerateOnlyWarningRequests() {
        //Given
        HttpErrorIssuer httpErrorIssuer = HttpErrorIssuer.with(0, 100);

        //When
        Optional<HttpErrorIssuer.ErrorIssuer> errorIssuer = httpErrorIssuer.randomIssuer();

        //Then
        Assertions.assertThat(errorIssuer).isPresent();
        Assertions.assertThat(errorIssuer.get().getErrorTags())
                .doesNotContainKey("error")
                .containsKey("http.status_code");
        Assertions.assertThat(errorIssuer.get().errorPercentage()).isEqualTo(100);
    }

}