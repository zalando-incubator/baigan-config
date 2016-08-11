package org.zalando.baigan.service;

import com.google.common.collect.ImmutableList;
import org.zalando.baigan.model.Configuration;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The ChainedConfigurationRepository provides a {@link ConfigurationRepository} that chains multiple configuration
 * repositories allowing you to fallback to the next repository when the higher order repositories fail to provide
 * a configuration value.
 * <p>
 * The first configuration repository will always be the first one to provide the value. In case it doesn't, this
 * configuration repository will advance to the next and repeat the attempt. It will continue to do so until it finds
 * a value or all the repositories failed.
 */
public class ChainedConfigurationRepository implements ConfigurationRepository {
    private final List<ConfigurationRepository> configurationRepositories;

    public ChainedConfigurationRepository(@Nonnull final ConfigurationRepository firstRepository,
                                          @Nonnull final ConfigurationRepository secondRepository,
                                          final ConfigurationRepository... moreRepositories) {
        checkNotNull(firstRepository, "firstRepository is required");
        checkNotNull(secondRepository, "secondRepository is required");

        ImmutableList.Builder<ConfigurationRepository> builder = ImmutableList.builder();
        builder.add(firstRepository);
        builder.add(secondRepository);
        if (moreRepositories != null && moreRepositories.length > 0) {
            builder.addAll(Arrays.asList(moreRepositories));
        }
        configurationRepositories = builder.build();
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

    }
}
