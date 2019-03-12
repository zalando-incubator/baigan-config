package org.zalando.baigan.spring.autoconfigure;

import org.zalando.baigan.ConfigurationStore;
import org.zalando.baigan.NamespacedConfigurationStore;
import org.zalando.baigan.etcd.EtcdStores;
import org.zalando.baigan.file.ConfigurationFile;
import org.zalando.baigan.file.FileStores;
import org.zalando.baigan.file.FileStores.StoreBuilder.SupplierBuilder.FormatBuilder;
import java.net.URI;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.zalando.baigan.ChainedConfigurationStore.chain;
import static org.zalando.baigan.LazyConfigurationStore.lazy;
import static org.zalando.baigan.etcd.EtcdStores.etcd;
import static org.zalando.baigan.s3.S3Stores.s3;
import static org.zalando.baigan.spring.autoconfigure.StoreProperties.Format.JSON;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

final class StoreBuilder {

    private static final Duration DEFAULT_CACHE = Duration.ofMinutes(2);
    private static final StoreProperties.Format DEFAULT_FORMAT = JSON;

    ConfigurationStore buildStore(final StoreProperties store) {
        switch (store.getType()) {
            case NAMESPACED:
                return buildLazy(store, () -> buildNamespaced(store));
            case CHAINED:
                return buildLazy(store, () -> buildChained(store));
            case LOCAL_FILE:
                return buildLazy(store, () -> buildLocalFile(store));
            case S3_FILE:
                return buildLazy(store, () -> buildS3File(store));
            case ETCD_FILE:
                return buildLazy(store, () -> buildEtcdFile(store));
            case ETCD:
                return buildLazy(store, () -> buildEtcd(store));
            default:
                throw new IllegalStateException("Unknown store type " + store.getType());
        }
    }

    private ConfigurationStore buildLazy(final StoreProperties store, final Supplier<ConfigurationStore> builder) {
        return orDefault(store.getLazy(), false) ? lazy(builder) : builder.get();
    }

    private ConfigurationStore buildNamespaced(final StoreProperties store) {
        final Map<String, ConfigurationStore> stores = store.getStores().entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), buildStore(entry.getValue())))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        return NamespacedConfigurationStore.forward(stores);
    }

    private ConfigurationStore buildChained(final StoreProperties store) {
        final List<ConfigurationStore> stores = store.getStores().values().stream()
                .map(this::buildStore)
                .collect(toList());
        return chain(stores);
    }

    private ConfigurationStore buildLocalFile(final StoreProperties store) {
        return buildFormat(store, buildFileCached(store).onLocalFile(Paths.get(store.getPath())));
    }

    private ConfigurationStore buildS3File(final StoreProperties store) {
        return buildFormat(store, buildFileCached(store).on(s3(store.getBucket(), store.getKey())));
    }

    private ConfigurationStore buildEtcdFile(final StoreProperties store) {
        return buildFormat(store, buildFileCached(store).on(etcd(URI.create(store.getUri()))));
    }

    private ConfigurationStore buildEtcd(final StoreProperties store) {
        return buildFormat(store, new FormatBuilderAdapter(buildEtcdCache(store).on(URI.create(store.getBaseUri()))));
    }

    private FileStores.StoreBuilder.SupplierBuilder buildFileCached(final StoreProperties store) {
        return FileStores.builder().cached(orDefault(store.getCache(), DEFAULT_CACHE));
    }

    private EtcdStores.StoreBuilder.UriBuilder buildEtcdCache(final StoreProperties store) {
        return EtcdStores.builder().cached(orDefault(store.getCache(), DEFAULT_CACHE));
    }

    private ConfigurationStore buildFormat(final StoreProperties store, final FormatBuilder builder) {
        switch (orDefault(store.getFormat(), DEFAULT_FORMAT)) {
            case JSON:
                return builder.asJson();
            case YAML:
                return builder.asYaml();
            default:
                throw new IllegalStateException("Unknown file format " + store.getFormat());
        }
    }

    private static <T> T orDefault(final T value, final T defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static class FormatBuilderAdapter implements FormatBuilder {

        private final EtcdStores.StoreBuilder.UriBuilder.FormatBuilder builder;

        FormatBuilderAdapter(final EtcdStores.StoreBuilder.UriBuilder.FormatBuilder builder) {
            this.builder = builder;
        }

        @Override
        public ConfigurationStore asJson() {
            return builder.asJson();
        }

        @Override
        public ConfigurationStore asYaml() {
            return builder.asYaml();
        }

        @Override
        public ConfigurationStore asFormat(final Function<String, ConfigurationFile> mapper) {
            throw new UnsupportedOperationException();
        }
    }

}
