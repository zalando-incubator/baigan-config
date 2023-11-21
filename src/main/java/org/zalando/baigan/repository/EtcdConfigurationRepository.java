package org.zalando.baigan.repository;

import com.google.common.base.Strings;
import org.zalando.baigan.model.Configuration;
import org.zalando.baigan.repository.etcd.service.EtcdClient;

import javax.annotation.Nonnull;
import java.io.UncheckedIOException;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A {@link ConfigurationRepository} implementation that loads the configuration from an etcd server.
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
        throw new UnsupportedOperationException("The put operation is not yet supported.");
    }

    /**
     * Gets the configuration value from an etcd server.
     * The configuration is not cached and is fetched from etcd on every request.
     *
     * @param key The key for which the configuration is to be fetched.
     * @return The configuration for the given key, if present. Empty, if the key
     * does not exist in etcd or fetching it from etcd fails.
     *
     * @throws UncheckedIOException if the configuration cannot be parsed.
     */
    @Override
    @Nonnull
    public Optional<Configuration> get(@Nonnull final String key) {
        checkArgument(!Strings.isNullOrEmpty(key),
                "Attempt to get configuration for an empty key !");
        final Optional<String> optionalConfig = etcdClient.get(CONFIG_PATH_PREFIX + key);

        return optionalConfig.flatMap(configurationParser::parseConfiguration);
    }
}
