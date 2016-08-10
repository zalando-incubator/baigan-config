package org.zalando.baigan.service;

import com.google.common.base.Optional;
import org.zalando.baigan.model.Configuration;

import javax.annotation.Nonnull;

/**
 * @author mchand
 */

public interface ConfigurationRepository {

    /**
     * This method required Guava's Optional which will be deprecated in favor of Java's Optional
     * It is being kept for backwards compatibility only
     *
     * @deprecated Override {@link #get(String)} instead.
     */
    @Nonnull
    @Deprecated
    Optional<Configuration<?>> getConfig(@Nonnull final String key);

    @Nonnull
    default java.util.Optional<Configuration<?>> get(@Nonnull final String key) {
        final Optional<Configuration<?>> config = getConfig(key);
        if (config.isPresent()) {
            return java.util.Optional.of(config.get());
        }
        return java.util.Optional.empty();
    }

    void put(@Nonnull final String key, @Nonnull final String value);

}
