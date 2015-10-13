package org.zalando.baigan.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.baigan.model.Configuration;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Implementation of {link ConfigurationRespository} supporting a file on
 * Classpath as the persistence storage for the Baigan configuration.
 *
 * @author mchand
 *
 */
public class FileSytemConfigurationRespository
        extends AbstractConfigurationRespository {

    private Logger LOG = LoggerFactory
            .getLogger(FileSytemConfigurationRespository.class);

    private final LoadingCache<String, Map<String, Configuration>> cachedConfigurations;

    private final String fileName;

    public FileSytemConfigurationRespository(long refreshIntervalInMinutes,
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

                        final Map<String, Configuration> configurationsMap = new HashMap<String, Configuration>();
                        configurations.stream()
                                .forEach(new Consumer<Configuration>() {
                            public void accept(Configuration each) {
                                configurationsMap.put(each.getAlias(), each);
                            };
                        });
                        return ImmutableMap.copyOf(configurationsMap);
                    }

                    @Override
                    public ListenableFuture<Map<String, Configuration>> reload(
                            String key, Map<String, Configuration> oldValue)
                                    throws Exception {
                        LOG.info("Reloading the configuration from file: "
                                + key);
                        return super.reload(key, oldValue);
                    }
                });
    }

    @Override
    public Optional<Configuration<?>> getConfig(String key) {
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
    public void put(String key, String value) {
        throw new UnsupportedOperationException();
    }

    public String loadResource(final String name) throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return Resources.toString(cl.getResource(name),
                Charset.forName("UTF-8"));

    }
}
