package org.zalando.baigan;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class NamespacedConfigurationStore implements ConfigurationStore {

    private final Map<String, ConfigurationStore> stores;

    public NamespacedConfigurationStore(final Map<String, ConfigurationStore> stores) {
        this.stores = new HashMap<>(stores);
    }

    @Override
    public <T> Optional<Configuration> getConfiguration(final String namespace, final String key) {
        final ConfigurationStore store = stores.get(namespace);
        if (store == null) {
            throw new IllegalStateException(String.format("No store registered for namespace [%s]", namespace));
        }
        return store.getConfiguration(namespace, key);
    }
}
