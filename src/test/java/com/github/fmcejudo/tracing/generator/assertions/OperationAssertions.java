package com.github.fmcejudo.tracing.generator.assertions;

import com.github.fmcejudo.tracing.generator.component.Component;
import com.github.fmcejudo.tracing.generator.task.Task;
import org.assertj.core.api.AbstractAssert;

import java.util.stream.Stream;

import static java.lang.String.join;

public class OperationAssertions extends AbstractAssert<OperationAssertions, Task> {

    private OperationAssertions(final Task task) {
        super(task, OperationAssertions.class);
    }

    public static OperationAssertions assertThat(final Task task) {
        return new OperationAssertions(task);
    }

    public OperationAssertions componentInstanceOf(final Class<? extends Component> clazz) {
        if (this.actual.getComponent() == null || !this.actual.getComponent().getClass().equals(clazz)) {
            failWithMessage("operation component is not instance of %s", clazz.getName());
        }
        return this;
    }

    public OperationAssertions dependsOnOperations(final Task... children) {
        if (this.actual.getChildTasks().size() == 0) {
            failWithMessage("operation does not dependant operations");
        }
        Stream.of(children).forEach(o -> {
            boolean present = this.actual.getChildTasks().stream().anyMatch(c ->
                    o.serviceName().equals(c.serviceName()) && c.getName().equals(o.getName())
            );

            if (!present) {
                failWithMessage(
                        "expected %s in %s to be amongst operation, but it is not",
                        o.getName(), o.serviceName()
                );
            }
        });
        return this;
    }

    public OperationAssertions doesNotDependsOnAny() {
        if (this.actual.getChildTasks().size() > 0) {
            failWithMessage(
                    "Children weren't expected in %s" ,
                    join("-", this.actual.getName(), this.actual.serviceName())
            );
        }
        return this;
    }
}
