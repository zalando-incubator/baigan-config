package org.zalando.baigan.e2e.structureds3repo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.StringJoiner;

public class SomeOtherConfigObject {
    private final String anotherConfigKey;

    @JsonCreator
    public SomeOtherConfigObject(@JsonProperty("other_config_key") String anotherConfigKey) {
        this.anotherConfigKey = anotherConfigKey;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SomeOtherConfigObject.class.getSimpleName() + "[", "]")
                .add("anotherConfigKey='" + anotherConfigKey + "'")
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SomeOtherConfigObject config = (SomeOtherConfigObject) o;
        return Objects.equals(anotherConfigKey, config.anotherConfigKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anotherConfigKey);
    }
}
