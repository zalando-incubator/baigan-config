package org.zalando.baigan.service;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.baigan.model.Configuration;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link ConfigurationRepository} supporting a file on
 * Classpath as the persistence storage for the Baigan configuration.
 *
 * @author mchand
 */
public class FileSystemConfigurationRepository
        extends AbstractConfigurationRepository {

    private final LoadingCache<String, Map<String, Configuration>> cachedConfigurations;
    private final String fileName;
    private Logger LOG = LoggerFactory
            .getLogger(FileSystemConfigurationRepository.class);

    public FileSystemConfigurationRepository(long refreshIntervalInMinutes,
                                             final String fileName) {
        this.fileName = fileName;

        cachedConfigurations = CacheBuilder.newBuilder()
                .refreshAfterWrite(refreshIntervalInMinutes, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Map<String, Configuration>>() {
                    @Override
                    public Map<String, Configuration> load(String key)
                            throws Exception {
                        final String configurationText = loadResource(key);
                        final Collection<Configuration> configurations = getConfigurations(
                                configurationText);

                        final ImmutableMap.Builder<String, Configuration> builder = ImmutableMap.builder();
                        for (Configuration each : configurations) {
                            builder.put(each.getAlias(), each);
                        }

                        return builder.build();
                    }

                    @Override
                    public ListenableFuture<Map<String, Configuration>> reload(
                            String key, Map<String, Configuration> oldValue)
                            throws Exception {
                        LOG.info("Reloading the configuration from file: " + key);
                        return super.reload(key, oldValue);
                    }
                });
    }

    @Nonnull
    @Override
    public Optional<Configuration<?>> getConfig(@Nonnull String key) {
        try {
            return Optional
                    .fromNullable(cachedConfigurations.get(fileName).get(key));
        } catch (ExecutionException e) {
            LOG.warn("Exception while trying to get configuration for key "
                    + key, e);
        }
        return Optional.absent();
    }

    @Override
    public void put(@Nonnull String key, @Nonnull String value) {
        throw new UnsupportedOperationException();
    }

    public String loadResource(final String file) throws IOException {
        final Path filePath = Paths.get(file);
        final String contents = new String(Files.readAllBytes(filePath));
        return contents;
    }
}
