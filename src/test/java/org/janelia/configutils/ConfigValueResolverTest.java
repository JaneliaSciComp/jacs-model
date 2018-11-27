package org.janelia.configutils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ConfigValueResolverTest {

    private ConfigValueResolver configValueResolver;

    @Before
    public void setUp() {
        configValueResolver = new ConfigValueResolver();
    }


    @Test
    public void resolveSuccessfully() {
        class TestData {
            private final String toResolve;
            private final Map<String, String> context;
            private final String expectedValue;

            private TestData(String toResolve,
                             Map<String, String> context,
                             String expectedValue) {
                this.toResolve = toResolve;
                this.context = context;
                this.expectedValue = expectedValue;
            }
        }

        TestData[] testData = new TestData[] {
                new TestData(null,
                        null,
                        null),
                new TestData("",
                        ImmutableMap.of("k1", "v1"),
                        ""),
                new TestData("{k1}",
                        ImmutableMap.of(),
                        "{k1}"),
                new TestData("value is this",
                        ImmutableMap.of("value is this", "not this"),
                        "value is this"),
                new TestData("{value is this}",
                        ImmutableMap.of("value is this", "not this"),
                        "not this"),
                new TestData("{k1}",
                        ImmutableMap.of("k1", "v1"),
                        "v1"),
                new TestData("value of {k1}",
                        ImmutableMap.of("k1", "v1"),
                        "value of v1"),
                new TestData("value of {k1}",
                        ImmutableMap.of("k1", "{k2}", "k2", "v2"),
                        "value of v2"),
                new TestData("value of {k1}",
                        ImmutableMap.of("k1", "{k2}"),
                        "value of {k2}"),
                new TestData("value of {k1} with {k2}",
                        ImmutableMap.of("k1", "{k2}", "k2", "v2"),
                        "value of v2 with v2"),
                new TestData("{C}",
                        ImmutableMap.of("A", "12345", "B", "{A}67890", "C", "{B} plus more"),
                        "1234567890 plus more"),
        };

        for (TestData td : testData) {
            assertEquals(td.expectedValue, configValueResolver.resolve(td.toResolve, td.context));
        }
    }

    @Test
    public void resolveWithExceptions() {
        class TestData {
            private final String toResolve;
            private final Map<String, String> context;
            private final Class<? extends Throwable> expectedExceptionClass;
            private final String expectedExceptionMessage;
            private final Set<String> expectedEvalHistory;

            private TestData(String toResolve,
                             Map<String, String> context,
                             Class<? extends Throwable> expectedExceptionClass,
                             String expectedExceptionMessage,
                             Set<String> expectedEvalHistory) {
                this.toResolve = toResolve;
                this.context = context;
                this.expectedExceptionClass = expectedExceptionClass;
                this.expectedExceptionMessage = expectedExceptionMessage;
                this.expectedEvalHistory = expectedEvalHistory;
            }
        }

        TestData[] testData = new TestData[] {
                new TestData("{",
                        ImmutableMap.of("k1", "v1"),
                        IllegalStateException.class,
                        "Unclosed placeholder found while trying to resolve { -> %s",
                        ImmutableSet.of()),
                new TestData("{k1",
                        ImmutableMap.of("k1", "v1"),
                        IllegalStateException.class,
                        "Unclosed placeholder found while trying to resolve {k1 -> %s",
                        ImmutableSet.of()),
                new TestData("{k1}",
                        ImmutableMap.of("k1", "{k2}", "k2", "{k1}"),
                        IllegalStateException.class,
                        "Circular dependency found while evaluating {k1} -> %s",
                        ImmutableSet.of("k1", "k2")),
                new TestData("value of {k1}",
                        ImmutableMap.of("k1", "{k2}", "k2", "{k1}"),
                        IllegalStateException.class,
                        "Circular dependency found while evaluating {k1} -> %s",
                        ImmutableSet.of("k1", "k2")),
        };

        for (TestData td : testData) {
            Assertions.assertThatThrownBy(() -> configValueResolver.resolve(td.toResolve, td.context))
                    .isInstanceOf(td.expectedExceptionClass)
                    .hasMessage(td.expectedExceptionMessage, td.expectedEvalHistory)
                    ;
        }
    }
}
