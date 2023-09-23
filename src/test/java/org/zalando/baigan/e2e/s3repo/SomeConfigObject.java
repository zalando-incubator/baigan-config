package org.zalando.baigan.e2e.s3repo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.StringJoiner;

public class SomeConfigObject {
    private final String configKey;

    @JsonCreator
    public SomeConfigObject(@JsonProperty("config_key") String configKey) {
        this.configKey = configKey;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SomeConfigObject.class.getSimpleName() + "[", "]")
                .add("configKey='" + configKey + "'")
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SomeConfigObject config = (SomeConfigObject) o;
        return Objects.equals(configKey, config.configKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configKey);
    }
}
