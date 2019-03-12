package org.zalando.baigan.spring.autoconfigure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.validation.Errors;
import org.zalando.baigan.ConfigurationStore;
import java.time.Duration;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.zalando.baigan.spring.autoconfigure.StoreProperties.Format.JSON;
import static org.zalando.baigan.spring.autoconfigure.StoreProperties.StoreType.CHAINED;
import static org.zalando.baigan.spring.autoconfigure.StoreProperties.StoreType.ETCD;
import static org.zalando.baigan.spring.autoconfigure.StoreProperties.StoreType.ETCD_FILE;
import static org.zalando.baigan.spring.autoconfigure.StoreProperties.StoreType.LOCAL_FILE;
import static org.zalando.baigan.spring.autoconfigure.StoreProperties.StoreType.NAMESPACED;
import static org.zalando.baigan.spring.autoconfigure.StoreProperties.StoreType.S3_FILE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

class StorePropertiesValidatorTest {

    private final Errors errors = mock(Errors.class);
    private final StorePropertiesValidator unit = new StorePropertiesValidator();

    @Test
    void supportsStoreProperties() {
        assertTrue(unit.supports(StoreProperties.class));
        assertFalse(unit.supports(ConfigurationStore.class));
    }

    @Test
    void requiresType() {
        unit.validate(new StoreProperties(), errors);
        verify(errors).rejectValue(eq("type"), anyString(), anyString());
    }

    @ParameterizedTest
    @MethodSource
    void validatesEachStoreType(final TestCase testCase) {

        final StoreProperties props = new StoreProperties();
        testCase.builder.accept(props);

        unit.validate(props, errors);

        testCase.unexpectedField.forEach(field -> verify(errors).rejectValue(eq(field), anyString(), contains("unexpected")));
        testCase.missingFields.forEach(field -> verify(errors).rejectValue(eq(field), anyString(), contains("required")));
        verifyNoMoreInteractions(errors);
    }

    @SuppressWarnings("unused")
    static Stream<TestCase> validatesEachStoreType() {
        return Stream.of(
                new TestCase(NAMESPACED, emptyList(), emptyList(), props -> {
                    props.setStores(emptyMap());
                }),
                new TestCase(NAMESPACED, asList("key", "path"), singletonList("stores"), props -> {
                    props.setKey("value");
                    props.setPath("value");
                }),

                new TestCase(CHAINED, emptyList(), emptyList(), props -> {
                    props.setStores(emptyMap());
                }),
                new TestCase(CHAINED, asList("key", "path"), singletonList("stores"), props -> {
                    props.setKey("value");
                    props.setPath("value");
                }),

                new TestCase(ETCD, emptyList(), emptyList(), props -> {
                    props.setBaseUri("value");
                }),
                new TestCase(ETCD, emptyList(), emptyList(), props -> {
                    props.setBaseUri("value");
                    props.setCache(Duration.ZERO);
                    props.setFormat(JSON);
                }),
                new TestCase(ETCD, singletonList("uri"), singletonList("baseUri"), props -> {
                    props.setUri("value");
                }),

                new TestCase(ETCD_FILE, emptyList(), emptyList(), props -> {
                    props.setUri("value");
                }),
                new TestCase(ETCD_FILE, emptyList(), emptyList(), props -> {
                    props.setUri("value");
                    props.setCache(Duration.ZERO);
                    props.setFormat(JSON);
                }),
                new TestCase(ETCD_FILE, singletonList("baseUri"), singletonList("uri"), props -> {
                    props.setBaseUri("value");
                }),

                new TestCase(LOCAL_FILE, emptyList(), emptyList(), props -> {
                    props.setPath("value");
                }),
                new TestCase(LOCAL_FILE, emptyList(), emptyList(), props -> {
                    props.setPath("value");
                    props.setCache(Duration.ZERO);
                    props.setFormat(JSON);
                }),
                new TestCase(LOCAL_FILE, singletonList("baseUri"), singletonList("path"), props -> {
                    props.setBaseUri("value");
                }),

                new TestCase(S3_FILE, emptyList(), emptyList(), props -> {
                    props.setBucket("value");
                    props.setKey("value");
                }),
                new TestCase(S3_FILE, emptyList(), emptyList(), props -> {
                    props.setBucket("value");
                    props.setKey("value");
                    props.setCache(Duration.ZERO);
                    props.setFormat(JSON);
                }),
                new TestCase(S3_FILE, singletonList("baseUri"), asList("bucket", "key"), props -> {
                    props.setBaseUri("value");
                })
        );
    }

    static class TestCase {
        final StoreProperties.StoreType type;
        final List<String> unexpectedField;
        final List<String> missingFields;
        final Consumer<StoreProperties> builder;

        TestCase(final StoreProperties.StoreType type,
                final List<String> unexpectedField,
                final List<String> missingFields,
                final Consumer<StoreProperties> builder) {
            this.type = type;
            this.unexpectedField = unexpectedField;
            this.missingFields = missingFields;
            this.builder = props -> {
                props.setType(type);
                builder.accept(props);
            };
        }

        @Override
        public String toString() {
            return new StringJoiner(", ")
                    .add("type=" + type)
                    .add("unexpected=" + unexpectedField)
                    .add("missing=" + missingFields)
                    .toString();
        }
    }
}