package org.zalando.baigan.repository;

import org.springframework.stereotype.Component;

/**
 * This class is provided as a Spring Bean. It allows creating {@link ConfigurationRepository} instances.
 * Each method returns a builder instance that can be used to configure the repository.
 */
@Component
public class RepositoryFactory {

    /**
     *  Allows creating a {@link S3ConfigurationRepository}.
     *
     * @return {@link S3ConfigurationRepositoryBuilder} Builder to create the repository
     */
    public S3ConfigurationRepositoryBuilder s3ConfigurationRepository() {
        return new S3ConfigurationRepositoryBuilder();
    }

    /**
     *  Allows creating a {@link FileSystemConfigurationRepository}.
     *
     * @return {@link FileSystemConfigurationRepositoryBuilder} Builder to create the repository
     */
    public FileSystemConfigurationRepositoryBuilder fileSystemConfigurationRepository() {
        return new FileSystemConfigurationRepositoryBuilder();
    }

    /**
     *  Allows creating a {@link EtcdConfigurationRepository}.
     *
     * @return {@link EtcdConfigurationRepositoryBuilder} Builder to create the repository
     */
    public EtcdConfigurationRepositoryBuilder etcdConfigurationRepository() {
        return new EtcdConfigurationRepositoryBuilder();
    }

    /**
     *  Allows creating a {@link ChainedConfigurationRepository}.
     *
     * @return {@link ChainedConfigurationRepositoryBuilder} Builder to create the repository
     */
    public ChainedConfigurationRepositoryBuilder chainedConfigurationRepository() {
        return new ChainedConfigurationRepositoryBuilder();
    }
}
