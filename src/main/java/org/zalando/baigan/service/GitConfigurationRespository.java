package org.zalando.baigan.service;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.baigan.model.Configuration;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

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
            return Optional.fromNullable(
                    cachedConfigurations.get(gitConfig.getRepoRefs()).get(key));
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

class GitConfig {
    private String repoRefs;
    private String repoName;
    private String repoOwner;
    private String gitHost;
    private String oauthToken;
    private String sourceFile;

    public GitConfig(String gitHost, String repoOwner, String repoName,
            String repoRefs, String sourceFile, String oauthToken) {
        this.repoRefs = repoRefs;
        this.repoName = repoName;
        this.repoOwner = repoOwner;
        this.gitHost = gitHost;
        this.oauthToken = oauthToken;
        this.sourceFile = sourceFile;
    }

    public String getRepoRefs() {
        return repoRefs;
    }

    public String getRepoName() {
        return repoName;
    }

    public String getRepoOwner() {
        return repoOwner;
    }

    public String getGitHost() {
        return gitHost;
    }

    public String getOauthToken() {
        return oauthToken;
    }

    public String getSourceFile() {
        return sourceFile;
    }

}
