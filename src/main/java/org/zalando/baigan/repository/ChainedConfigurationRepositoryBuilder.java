package org.zalando.baigan.repository;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.Assert.notEmpty;

/**
 * A builder to construct a {@link FileSystemConfigurationRepository}.
 * <p>
 * At least one repository has to be provided.
 */
public class ChainedConfigurationRepositoryBuilder {

    final List<ConfigurationRepository> configurationRepositories = new ArrayList<>();

    ChainedConfigurationRepositoryBuilder() {

    }

    /**
     * Adds a {@link ConfigurationRepository} after the previously added repositories.
     * <p>
     * @param configurationRepository The repository to add.
     */
    public ChainedConfigurationRepositoryBuilder addRepository(final ConfigurationRepository configurationRepository) {
        configurationRepositories.add(configurationRepository);
        return this;
    }

    /**
     * Adds all provided {@link ConfigurationRepository} instances in the order they are provided,
     * placing them after the previously added repositories.
     * <p>
     * @param configurationRepositories The list of repositories to add.
     */
    public ChainedConfigurationRepositoryBuilder addRepositories(final List<ConfigurationRepository> configurationRepositories) {
        this.configurationRepositories.addAll(configurationRepositories);
        return this;
    }

    public ChainedConfigurationRepository build() {
        notEmpty(configurationRepositories, "configurationRepositories must not be empty");
        return new ChainedConfigurationRepository(configurationRepositories);
    }
}
