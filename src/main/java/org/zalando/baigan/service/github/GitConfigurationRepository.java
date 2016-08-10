package org.zalando.baigan.service.github;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.baigan.model.Configuration;
import org.zalando.baigan.service.AbstractConfigurationRepository;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {link ConfigurationRepository} supporting Github as
 * the persistence storage for the baigan configuration.
 *
 * @author mchand
 */
public class GitConfigurationRepository
        extends AbstractConfigurationRepository {

    private final LoadingCache<String, Map<String, Configuration>> cachedConfigurations;
    private final GitConfig gitConfig;
    private Logger LOG = LoggerFactory
            .getLogger(GitConfigurationRepository.class);

    public GitConfigurationRepository(long refreshIntervalInMinutes, GitConfig gitConfig) {
        this.gitConfig = gitConfig;
        cachedConfigurations = CacheBuilder.newBuilder()
                .refreshAfterWrite(refreshIntervalInMinutes, TimeUnit.MINUTES)
                .build(new GitCacheLoader(gitConfig));
    }

    @Nonnull
    @Override
    public Optional<Configuration<?>> getConfig(@Nonnull String key) {
        try {
            return Optional.fromNullable(cachedConfigurations
                    .get(gitConfig.getSourceFile()).get(key));
        } catch (ExecutionException e) {
            LOG.warn("Exception while trying to get configuration for key "
                    + key, e);
        }
        return Optional.absent();
    }

    @Override
    public void put(@Nonnull String key, @Nonnull String value) {
        throw new UnsupportedOperationException();
    }

}
