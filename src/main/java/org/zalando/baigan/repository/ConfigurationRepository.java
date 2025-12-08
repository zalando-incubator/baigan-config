package org.zalando.baigan.repository;

import org.zalando.baigan.model.Configuration;

import jakarta.annotation.Nonnull;
import java.util.Optional;

/**
 * The {@link ConfigurationRepository} provides the actual configuration values. A Spring Bean of this type
 * has to be available in the ApplicationContext. A {@link ConfigurationRepository} can be constructed using
 * the Spring Bean of type {@link RepositoryFactory}.
 */
public interface ConfigurationRepository {

    @Nonnull
    Optional<Configuration> get(@Nonnull final String key);

    void put(@Nonnull final String key, @Nonnull final String value);
}
