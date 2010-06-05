package no.muda.jackbox;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Arrays;

import no.muda.jackbox.example.ExampleDependency;
import no.muda.jackbox.example.ExampleRecordedObject;

import org.junit.Before;
import org.junit.Test;

public class JackboxReplayTest {

    private MethodRecording recordedExampleMethodWith2And2;

    @Before
    public void setupRecording() throws Exception {
        recordedExampleMethodWith2And2 =
            new MethodRecording(ExampleRecordedObject.class,
                    ExampleRecordedObject.class.getMethod("exampleMethod", Integer.TYPE, Integer.TYPE),
                    Arrays.asList(2, 2));
    }

    @Test
    public void shouldNotThrowExceptionWhenInvocationIsUnchanged() throws Exception {
        recordedExampleMethodWith2And2.setReturnValue(4);
        recordedExampleMethodWith2And2.replay();
    }

    @Test
    public void shouldThrowExceptionIfReturnValueChanges() throws Exception {
        recordedExampleMethodWith2And2.setReturnValue(5);

        boolean threwException = true;
        try {
            recordedExampleMethodWith2And2.replay();
            threwException = false;
        } catch (AssertionError e) {
            assertThat(e.getMessage())
                .contains("expected <5>")
                .contains("got <4>")
                .contains("exampleMethod");
            threwException = true;
        }

        assertThat(threwException).describedAs("Should throw when return changes").isTrue();
    }

    @Test
    public void shouldReplayDelegatedObject() throws Exception {
        String recordedReturnValueFromDependencyMethod = "foo bar baz";

        MethodRecording methodRecording = new MethodRecording(ExampleRecordedObject.class,
                ExampleRecordedObject.class.getMethod("exampleMethodThatDelegatesToDependency", String.class),
                Arrays.asList("abcd"));
        methodRecording.setReturnValue(recordedReturnValueFromDependencyMethod);

        MethodRecording dependencyMethodRecording = new MethodRecording(ExampleDependency.class,
                ExampleDependency.class.getMethod("invokedMethodOnDependency", String.class),
                Arrays.asList("abcd"));
        dependencyMethodRecording.setReturnValue(recordedReturnValueFromDependencyMethod);

        DependencyRecording dependencyRecording = new DependencyRecording(ExampleDependency.class);
        dependencyRecording.addMethodRecording(dependencyMethodRecording);
        methodRecording.addDependencyRecording(dependencyRecording);

        methodRecording.replay();
    }


}
