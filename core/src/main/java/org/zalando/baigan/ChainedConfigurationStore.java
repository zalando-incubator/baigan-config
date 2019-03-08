package org.zalando.baigan;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

public final class ChainedConfigurationStore implements ConfigurationStore {

    public static ConfigurationStore chain(final ConfigurationStore store, final ConfigurationStore... stores) {
        return new ChainedConfigurationStore(concat(of(store), of(stores)).collect(toList()));
    }

    public static ConfigurationStore chain(final List<ConfigurationStore> stores) {
        if (stores.isEmpty()) {
            throw new IllegalArgumentException("At least one store must be given");
        }
        return new ChainedConfigurationStore(stores);
    }

    private final List<ConfigurationStore> stores;

    private ChainedConfigurationStore(List<ConfigurationStore> stores) {
        this.stores = stores;
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
