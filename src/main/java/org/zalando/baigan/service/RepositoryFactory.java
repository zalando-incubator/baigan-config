package org.zalando.baigan.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.baigan.service.aws.S3ConfigurationRepositoryBuilder;

import java.util.List;

@Component
public class RepositoryFactory {

    private final ConfigurationParser configurationParser;

    @Autowired
    public RepositoryFactory(final ConfigurationParser configurationParser) {
        this.configurationParser = configurationParser;
    }

    public S3ConfigurationRepositoryBuilder s3ConfigurationRepository() {
        return new S3ConfigurationRepositoryBuilder(configurationParser);
    }

    public FileSystemConfigurationRepositoryBuilder fileSystemConfigurationRepository() {
        return new FileSystemConfigurationRepositoryBuilder(configurationParser);
    }

    public EtcdConfigurationRepository etcdConfigurationRepository(String etcdUrl) {
        return new EtcdConfigurationRepository(etcdUrl, configurationParser);
    }

    public ChainedConfigurationRepository chainedConfigurationRepository(List<ConfigurationRepository> configurationRepositories) {
        if (configurationRepositories.size() == 2) {
            return new ChainedConfigurationRepository(configurationRepositories.get(0), configurationRepositories.get(1));
        } else if (configurationRepositories.size() > 2) {
            return new ChainedConfigurationRepository(
                    configurationRepositories.get(0),
                    configurationRepositories.get(1),
                    configurationRepositories.subList(2, configurationRepositories.size()).toArray(new ConfigurationRepository[0])
            );
        } else {
            throw new IllegalArgumentException("ChainedConfigurationRepository requires at least two repositories");
        }
    }
}
