package org.zalando.baigan.repository;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.baigan.model.Configuration;
import org.zalando.baigan.repository.etcd.service.EtcdClient;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A {@link ConfigurationRepository} implementation that loads the configuration from an etcd server.
 * The configuration is not cached and is fetched from etcd on every request.
 */
public class EtcdConfigurationRepository extends AbstractConfigurationRepository {

    private static final String CONFIG_PATH_PREFIX = "/v2/keys/";
    private static final Logger LOG = LoggerFactory.getLogger(EtcdConfigurationRepository.class);
    private final EtcdClient etcdClient;

    EtcdConfigurationRepository(final String etcdUrl) {
        etcdClient = new EtcdClient(etcdUrl);
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
