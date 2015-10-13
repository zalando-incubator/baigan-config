package org.zalando.baigan.service.github;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.baigan.model.Configuration;
import org.zalando.baigan.service.AbstractConfigurationRespository;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

/**
 * Implementation of {link ConfigurationRespository} supporting the Github as
 * the persistence storage for the baigan configuration.
 *
 * @author mchand
 *
 */
public class GitConfigurationRespository
        extends AbstractConfigurationRespository {

    private Logger LOG = LoggerFactory
            .getLogger(GitConfigurationRespository.class);

    private final LoadingCache<String, Map<String, Configuration>> cachedConfigurations;
    private final GitConfig gitConfig;

    public GitConfigurationRespository(long refreshIntervalInMinutes,
            GitConfig gitConfig) {
        this.gitConfig = gitConfig;
        cachedConfigurations = CacheBuilder.newBuilder()
                .refreshAfterWrite(refreshIntervalInMinutes, TimeUnit.MINUTES)
                .build(new GitCacheLoader(gitConfig));
    }

    @Override
    public Optional<Configuration<?>> getConfig(String key) {
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
    public void put(String key, String value) {
        throw new UnsupportedOperationException();
    }

}
