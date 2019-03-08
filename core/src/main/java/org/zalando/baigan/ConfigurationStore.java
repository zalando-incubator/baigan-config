package org.zalando.baigan;

import java.util.Optional;

public interface ConfigurationStore {

    @SuppressWarnings("unchecked")
    default <T> Optional<Configuration<T>> getTypedConfiguration(final String namespace, final String key) {
        return getConfiguration(namespace, key).map(a -> (Configuration<T>) a);
    }

    <T> Optional<Configuration> getConfiguration(final String namespace, final String key);
}
