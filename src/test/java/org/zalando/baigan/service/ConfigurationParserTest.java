package org.zalando.baigan.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.zalando.baigan.model.Condition;
import org.zalando.baigan.model.Configuration;
import org.zalando.baigan.model.Equals;
import org.zalando.baigan.proxy.BaiganConfigClasses;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThrows;

public class ConfigurationParserTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final BaiganConfigClasses stringConfigClasses = new BaiganConfigClasses(Map.of("some.config.some.key", String.class));
    private static final ConfigurationParser parser = new ConfigurationParser(stringConfigClasses, objectMapper);

    @Test
    public void whenInputContainsKeyForKnownType_shouldParseConfiguration() {
        final String input = "[{\"alias\":\"some.config.some.key\",\"defaultValue\":\"someValue\"}]";

        final List<Configuration<?>> parsedConfigs = parser.getConfigurations(input);

        final List<Configuration<String>> expectedConfigs = List.of(
                new Configuration<>("some.config.some.key", null, Set.of(), "someValue")
        );

        assertThat(parsedConfigs, equalTo(expectedConfigs));
    }

    @Test
    public void whenInputContainsTwoConfigs_shouldParseAllConfigsConfiguration() {
        final BaiganConfigClasses structuredConfigClasses = new BaiganConfigClasses(Map.of(
                "some.config.some.key", String.class,
                "some.struct.config", StructuredConfig.class
        ));
        final ConfigurationParser parser = new ConfigurationParser(structuredConfigClasses, objectMapper);

        final String input = "[{\"alias\":\"some.config.some.key\",\"defaultValue\":\"someValue\"}," +
                "{\"alias\":\"some.struct.config\",\"defaultValue\":{\"someConfig\":\"some value\",\"someOtherConfig\":1}}]";

        final List<Configuration<?>> parsedConfigs = parser.getConfigurations(input);

        final List<Configuration<?>> expectedConfigs = List.of(
                new Configuration<>("some.config.some.key", null, Set.of(), "someValue"),
                new Configuration<>("some.struct.config", null, Set.of(), new StructuredConfig("some value", 1))
        );

        assertThat(parsedConfigs, equalTo(expectedConfigs));
    }

    @Test
    public void whenInputIsNullOrEmpty_shouldReturnEmptyList() {
        assertThat(parser.getConfigurations(null), equalTo(List.of()));
        assertThat(parser.getConfigurations(""), equalTo(List.of()));
    }

    @Test
    public void whenInputIsEmptyArray_shouldReturnEmptyList() {
        assertThat(parser.getConfigurations("[]"), equalTo(List.of()));
    }

    @Test
    public void whenConfigKeyInInputHasNoClassMapping_shouldIgnoreKey() {
        final String input = "[{\"alias\":\"some.config.some.key\",\"defaultValue\":\"someValue\"}," +
                "{\"alias\":\"some.missing.config.key\",\"defaultValue\":\"some other value\"}]";

        final List<Configuration<?>> parsedConfigs = parser.getConfigurations(input);

        assertThat(parsedConfigs.stream().map(config -> (String) config.getDefaultValue()).collect(toList()), equalTo(List.of("someValue")));
    }

    @Test
    public void whenInputCannotBeParsedToJson_shouldThrowException() {
        final String input = "some invalid input";

        assertThrows(UncheckedIOException.class, () -> parser.getConfigurations(input));
    }

    @Test
    public void whenInputIsStructuredConfigWithConditions_shouldDeserializeTypedConfigurationWithConditions() {
        final BaiganConfigClasses structuredConfigClasses = new BaiganConfigClasses(Map.of("some.config.some.key", StructuredConfig.class));
        final ConfigurationParser parser = new ConfigurationParser(structuredConfigClasses, objectMapper);

        final String input = "[{\"alias\":\"some.config.some.key\",\"description\":\"a description\"," +
                "\"defaultValue\":{\"someConfig\":\"some value\",\"someOtherConfig\":1}," +
                "\"conditions\":[{\"paramName\":\"some param name\",\"conditionType\":{\"type\":\"Equals\",\"onValue\":\"some value\"}," +
                "\"value\":{\"someConfig\":\"some conditional value\",\"someOtherConfig\":-1}}]}]";

        final List<Configuration<?>> parsedConfigs = parser.getConfigurations(input);

        final Configuration<StructuredConfig> expectedConfig = new Configuration<>(
                "some.config.some.key",
                "a description",
                Set.of(new Condition<>("some param name", new Equals("some value"), new StructuredConfig("some conditional value", -1))),
                new StructuredConfig("some value", 1)
        );

        assertThat(parsedConfigs, equalTo(List.of(expectedConfig)));
    }

    @Test
    public void whenDefaultValueCannotBeParsed_shouldThrowException() {
        final String input = "[{\"alias\":\"some.config.some.key\",\"defaultValue\":{}}]";

        assertThrows(RuntimeException.class, () -> parser.getConfigurations(input));
    }

    @Test
    public void whenConditionValueCannotBeParsed_shouldThrowException() {
        final String input = "[{\"alias\":\"some.config.some.key\",\"defaultValue\":\"some value\"," +
                "\"conditions\":[{\"paramName\":\"some param name\",\"conditionType\":{\"type\":\"Equals\",\"onValue\":\"some value\"},\"value\":{}}]}]";

        assertThrows(RuntimeException.class, () -> parser.getConfigurations(input));
    }

    private static class StructuredConfig {
        private final String someConfig;
        private final int someOtherConfig;

        @JsonCreator
        private StructuredConfig(@JsonProperty("someConfig") String someConfig, @JsonProperty("someOtherConfig") int someOtherConfig) {
            this.someConfig = someConfig;
            this.someOtherConfig = someOtherConfig;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StructuredConfig that = (StructuredConfig) o;
            return someOtherConfig == that.someOtherConfig && Objects.equals(someConfig, that.someConfig);
        }

        @Override
        public int hashCode() {
            return Objects.hash(someConfig, someOtherConfig);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", StructuredConfig.class.getSimpleName() + "[", "]")
                    .add("someConfig='" + someConfig + "'")
                    .add("someOtherConfig=" + someOtherConfig)
                    .toString();
        }
    }
}
