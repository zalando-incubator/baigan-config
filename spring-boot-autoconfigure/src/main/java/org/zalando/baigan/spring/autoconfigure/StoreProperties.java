package org.zalando.baigan.spring.autoconfigure;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

@SuppressWarnings("WeakerAccess")
final class StoreProperties {

    enum StoreType {
        NAMESPACED(singletonList("stores"), singletonList("lazy")),
        CHAINED(singletonList("stores"), singletonList("lazy")),
        LOCAL_FILE(singletonList("path"), asList("lazy", "cache", "format")),
        S3_FILE(asList("bucket", "key"), asList("lazy", "cache", "format")),
        ETCD_FILE(singletonList("uri"), asList("lazy", "cache", "format")),
        ETCD(singletonList("baseUri"), asList("lazy", "cache", "format"));

        private final List<String> requiredFields;
        private final List<String> optionalFields;

        StoreType(final List<String> requiredFields) {
            this(requiredFields, singletonList("lazy"));
        }

        StoreType(final List<String> requiredFields, final List<String> optionalFields) {
            this.requiredFields = concat(of("type"), requiredFields.stream()).collect(toList());
            this.optionalFields = optionalFields;
        }

        List<String> getRequiredFields() {
            return requiredFields;
        }

        List<String> getOptionalFields() {
            return optionalFields;
        }
    }

    enum Format {
        JSON,
        YAML
    }

    StoreType type;
    Boolean lazy;
    Duration cache;
    String path;
    String bucket;
    String key;
    Format format;
    String uri;
    String baseUri;
    Map<String, StoreProperties> stores;

    public StoreType getType() {
        return type;
    }

    public void setType(final StoreType type) {
        this.type = type;
    }

    public Boolean getLazy() {
        return lazy;
    }

    public void setLazy(final Boolean lazy) {
        this.lazy = lazy;
    }

    public Duration getCache() {
        return cache;
    }

    public void setCache(final Duration cache) {
        this.cache = cache;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(final Format format) {
        this.format = format;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(final String uri) {
        this.uri = uri;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(final String baseUri) {
        this.baseUri = baseUri;
    }

    public Map<String, StoreProperties> getStores() {
        return stores;
    }

    public void setStores(final Map<String, StoreProperties> stores) {
        this.stores = stores;
    }

}
