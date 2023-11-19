package org.zalando.baigan.repository;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.baigan.model.Configuration;
import org.zalando.baigan.repository.etcd.service.EtcdClient;

import javax.annotation.Nonnull;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A {@link ConfigurationRepository} implementation that loads the configuration from an etcd server.
 * The configuration is not cached and is fetched from etcd on every request.
 */
// TODO Upgrade to v3
public class EtcdConfigurationRepository implements ConfigurationRepository {

    private static final String CONFIG_PATH_PREFIX = "/v2/keys/";
    private final EtcdClient etcdClient;
    private final ConfigurationParser configurationParser;

    EtcdConfigurationRepository(final String etcdUrl, final ConfigurationParser configurationParser) {
        this.etcdClient = new EtcdClient(etcdUrl);
        this.configurationParser = configurationParser;
    }

    @Override
    public void put(@Nonnull final String key, @Nonnull final String value) {
        throw new UnsupportedOperationException(
                "The put operation is not yet supported.");
    }

    @Override
    @Nonnull
    public Optional<Configuration> get(@Nonnull final String key) {
        checkArgument(!Strings.isNullOrEmpty(key),
                "Attempt to get configuration for an empty key !");
        final Optional<String> optionalConfig = etcdClient.get(CONFIG_PATH_PREFIX + key);

        return optionalConfig.flatMap(configurationParser::parseConfiguration);
    }
}
