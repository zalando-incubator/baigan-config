package org.zalando.baigan.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public EtcdConfigurationRepositoryBuilder etcdConfigurationRepository() {
        return new EtcdConfigurationRepositoryBuilder(configurationParser);
    }

    public ChainedConfigurationRepositoryBuilder chainedConfigurationRepository() {
        return new ChainedConfigurationRepositoryBuilder();
    }
}
