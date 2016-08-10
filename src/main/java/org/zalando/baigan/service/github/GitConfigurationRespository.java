package org.zalando.baigan.service.github;

/**
 * This class had a typo in its name and is being kept for backwards compatibility only
 *
 * @deprecated use {@link GitConfigurationRepository} instead.
 */
@Deprecated
public final class GitConfigurationRespository extends GitConfigurationRepository {

    @Deprecated
    public GitConfigurationRespository(long refreshIntervalInMinutes, GitConfig gitConfig) {
        super(refreshIntervalInMinutes, gitConfig);
    }
}
