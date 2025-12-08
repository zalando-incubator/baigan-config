package org.zalando.baigan.repository;

import org.zalando.baigan.model.Configuration;

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * The ChainedConfigurationRepository provides a {@link ConfigurationRepository} that chains multiple configuration
 * repositories. Repositories are checked in the order they are provided for the requested key.
 * The first repository that contains the key is used to provide the value.
 */
public class ChainedConfigurationRepository implements ConfigurationRepository {
    private final List<ConfigurationRepository> configurationRepositories;

    ChainedConfigurationRepository(final List<ConfigurationRepository> configurationRepositories) {
        this.configurationRepositories = configurationRepositories;
    }

    @Nonnull
    @Override
    public Optional<Configuration> get(@Nonnull String key) {
        for (final ConfigurationRepository configurationRepository : configurationRepositories) {
            final Optional<Configuration> configuration = configurationRepository.get(key);
            if (configuration.isPresent()) {
                return configuration;
            }
        }
        return Optional.empty();
    }

    @Override
    public void put(@Nonnull String key, @Nonnull String value) {
        throw new UnsupportedOperationException("The ChainedConfigurationRepository doesn't allow any changes.");
    }
}
