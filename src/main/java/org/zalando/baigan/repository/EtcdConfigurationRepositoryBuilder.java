package org.zalando.baigan.repository;

import static java.util.Objects.requireNonNull;

/**
 * A builder to construct an {@link EtcdConfigurationRepository}.
 * <p>
 * Requires that at least {@link EtcdConfigurationRepositoryBuilder::etcdUrl} is specified.
 */
public class EtcdConfigurationRepositoryBuilder {

    private String etcdUrl;

    EtcdConfigurationRepositoryBuilder() {

    }

    public EtcdConfigurationRepositoryBuilder etcdUrl(final String etcdUrl) {
        this.etcdUrl = etcdUrl;
        return this;
    }

    public EtcdConfigurationRepository build() {
        requireNonNull(etcdUrl, "etcdUrl must not be null");
        return new EtcdConfigurationRepository(etcdUrl);
    }
}
