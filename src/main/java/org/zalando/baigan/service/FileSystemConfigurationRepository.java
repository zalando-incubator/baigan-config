package org.zalando.baigan.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.zalando.baigan.model.Configuration;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link ConfigurationRepository} supporting a file on
 * Classpath as the persistence storage for the Baigan configuration.
 *
 * @author mchand
 */
public class FileSystemConfigurationRepository implements ConfigurationRepository, ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemConfigurationRepository.class);

    private ConfigurationParser configurationParser;
    private LoadingCache<String, Map<String, Configuration<?>>> cachedConfigurations;
    private final String fileName;
    private final long refreshIntervalInSeconds;

     FileSystemConfigurationRepository(final String fileName, long refreshIntervalInSeconds) {
        this.fileName = fileName;
        this.refreshIntervalInSeconds = refreshIntervalInSeconds;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.configurationParser = applicationContext.getBean(ConfigurationParser.class);

        cachedConfigurations = CacheBuilder.newBuilder()
                .refreshAfterWrite(refreshIntervalInSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<>() {
                    @Override
                    public Map<String, Configuration<?>> load(String filename) {
                        try {
                            return loadConfigurations(filename);
                        } catch (final Exception e) {
                            LOG.error("Failed to refresh configuration, keeping old state.", e);
                            throw e;
                        }
                    }

                    @Override
                    public ListenableFuture<Map<String, Configuration<?>>> reload(
                            String key, Map<String, Configuration<?>> oldValue)
                            throws Exception {
                        LOG.info("Reloading the configuration from file [{}]", key);
                        return super.reload(key, oldValue);
                    }
                });
        cachedConfigurations.put(fileName, loadConfigurations(fileName));
    }

    @Nonnull
    @Override
    public Optional<Configuration> get(@Nonnull String key) {
        try {
            return Optional.ofNullable(cachedConfigurations.get(fileName).get(key));
        } catch (ExecutionException e) {
            throw new RuntimeException("Exception while trying to get configuration for key " + key, e);
        }
    }

    @Override
    public void put(@Nonnull String key, @Nonnull String value) {
        throw new UnsupportedOperationException();
    }


    protected Map<String, Configuration<?>> loadConfigurations(String filename) {
        final String configurationText = loadResource(filename);
        final Collection<Configuration<?>> configurations = configurationParser.getConfigurations(
                configurationText);

        final ImmutableMap.Builder<String, Configuration<?>> builder = ImmutableMap.builder();
        for (Configuration<?> each : configurations) {
            builder.put(each.getAlias(), each);
        }

        return builder.build();
    }

    protected String loadResource(final String file) {
        try {
            final Path filePath = Paths.get(file);
            return new String(Files.readAllBytes(filePath));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
