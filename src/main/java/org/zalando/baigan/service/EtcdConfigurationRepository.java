package org.zalando.baigan.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.baigan.etcd.service.EtcdClient;
import org.zalando.baigan.model.Configuration;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author mchand
 */
// TODO Upgrade to v3 and add E2E test
public class EtcdConfigurationRepository {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new GuavaModule());
    private static final String ETCD_URL_ENV_NAME = "ETCD_URL";
    private static final String CONFIG_PATH_PREFIX = "/v2/keys/";
    private Logger LOG = LoggerFactory.getLogger(EtcdConfigurationRepository.class);
    private EtcdClient etcdClient;

    @VisibleForTesting
    public EtcdConfigurationRepository(final EtcdClient etcdClient) {
        checkArgument(etcdClient != null);
        this.etcdClient = etcdClient;

    }

    public EtcdConfigurationRepository() {
        etcdClient = new EtcdClient(getUrl());
    }

    private String getUrl() {
        String systemEtcdUrl = System.getenv(ETCD_URL_ENV_NAME);
        if (Strings.isNullOrEmpty(systemEtcdUrl)) {
            LOG.error("$" + ETCD_URL_ENV_NAME
                    + " is undefined. This is required in order to by the baigan configuration service.");
        }
        return systemEtcdUrl;
    }

    public void put(@Nonnull final String key, @Nonnull final String value) {
        throw new UnsupportedOperationException(
                "The put operation is not yet supported.");
    }

    @Nonnull
    public Optional<Configuration> get(@Nonnull final String key) {
        try {
            checkArgument(!Strings.isNullOrEmpty(key),
                    "Attempt to get configuration for an empty key !");
            final Optional<String> optionalConfig = etcdClient.get(CONFIG_PATH_PREFIX + key);

            if (optionalConfig.isPresent()) {
                return Optional.of(objectMapper.readValue(optionalConfig.get(),
                        Configuration.class));
            }

        } catch (IOException e) {
            LOG.warn("Error while loading configuration for key: " + key, e);
        }
        return Optional.empty();
    }
}
