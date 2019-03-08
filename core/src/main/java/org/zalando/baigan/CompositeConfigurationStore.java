package org.zalando.baigan;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

public final class CompositeConfigurationStore implements ConfigurationStore {

    private final List<ConfigurationStore> stores;

    public CompositeConfigurationStore(final ConfigurationStore store, final ConfigurationStore... stores) {
        this.stores = concat(of(store), of(stores)).collect(toList());
    }

    @Override
    public <T> Optional<Configuration> getConfiguration(final String namespace, final String key) {
        for (ConfigurationStore store : stores) {
            final Optional<Configuration> configuration = store.getConfiguration(namespace, key);
            if (configuration.isPresent()) {
                return configuration;
            }
        }
        return Optional.empty();
    }
}
