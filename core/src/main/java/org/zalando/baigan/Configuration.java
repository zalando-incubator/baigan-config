package org.zalando.baigan;

import static java.util.Objects.requireNonNull;

public final class Configuration<T> {

    private final String key;
    private final String description;
    private final T value;

    public Configuration(final String key, final String description, final T value) {
        this.key = requireNonNull(key);
        this.description = requireNonNull(description);
        this.value = requireNonNull(value);
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    public T getValue() {
        return value;
    }
}
