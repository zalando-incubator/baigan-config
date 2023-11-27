package org.zalando.baigan.repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import static java.util.Objects.requireNonNull;

/**
 * A builder to construct an {@link EtcdConfigurationRepository}.
 * <p>
 * Requires that at least {@link EtcdConfigurationRepositoryBuilder#etcdUrl(String)} is specified.
 */
public class EtcdConfigurationRepositoryBuilder {

    private String etcdUrl;
    private ObjectMapper objectMapper;
    private final ConfigurationParser configurationParser;

    EtcdConfigurationRepositoryBuilder(final ConfigurationParser configurationParser) {
        this.configurationParser = configurationParser;
    }

    public EtcdConfigurationRepositoryBuilder etcdUrl(final String etcdUrl) {
        this.etcdUrl = etcdUrl;
        return this;
    }

    /**
     * @param objectMapper The {@link ObjectMapper} used to parse the configurations.
     */
    public EtcdConfigurationRepositoryBuilder objectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    public EtcdConfigurationRepository build() {
        requireNonNull(etcdUrl, "etcdUrl must not be null");

        if (objectMapper != null) {
            configurationParser.setObjectMapper(objectMapper);
        }

        return new EtcdConfigurationRepository(etcdUrl, configurationParser);
    }
}
