package org.zalando.baigan.service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.baigan.etcd.service.EtcdClient;
import org.zalando.baigan.model.Configuration;

import javax.annotation.Nonnull;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author mchand
 */
// TODO Upgrade to v3
public class EtcdConfigurationRepository implements ConfigurationRepository {

    private static final String ETCD_URL_ENV_NAME = "ETCD_URL";
    private static final String CONFIG_PATH_PREFIX = "/v2/keys/";
    private final Logger LOG = LoggerFactory.getLogger(EtcdConfigurationRepository.class);
    private final EtcdClient etcdClient;
    private final ConfigurationParser configurationParser;

    @VisibleForTesting
    EtcdConfigurationRepository(final EtcdClient etcdClient, final ConfigurationParser configurationParser) {
        checkArgument(etcdClient != null);
        this.etcdClient = etcdClient;
        this.configurationParser = configurationParser;

    }

    EtcdConfigurationRepository(final ConfigurationParser configurationParser) {
        this.configurationParser = configurationParser;
        this.etcdClient = new EtcdClient(getUrl());
    }

    EtcdConfigurationRepository(final String etcdUrl, final ConfigurationParser configurationParser) {
        this.configurationParser = configurationParser;
        this.etcdClient = new EtcdClient(etcdUrl);
    }

    private String getUrl() {
        String systemEtcdUrl = System.getenv(ETCD_URL_ENV_NAME);
        if (Strings.isNullOrEmpty(systemEtcdUrl)) {
            LOG.error("$" + ETCD_URL_ENV_NAME
                    + " is undefined. This is required by the baigan configuration service.");
        }
        return systemEtcdUrl;
    }

    public void put(@Nonnull final String key, @Nonnull final String value) {
        throw new UnsupportedOperationException(
                "The put operation is not yet supported.");
    }

    @Nonnull
    public Optional<Configuration> get(@Nonnull final String key) {
        checkArgument(!Strings.isNullOrEmpty(key),
                "Attempt to get configuration for an empty key !");
        final Optional<String> optionalConfig = etcdClient.get(CONFIG_PATH_PREFIX + key);

        return optionalConfig.flatMap(configurationParser::parseConfiguration);
    }
}
