package org.zalando.baigan.service;

import com.google.common.base.Optional;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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
    default Optional<Configuration> getConfig(@Nonnull final String key) {
        return get(key).map(Optional::of).orElse(Optional.absent());
    }

    @Nonnull
    java.util.Optional<Configuration> get(@Nonnull final String key);

    void put(@Nonnull final String key, @Nonnull final String value);
}
