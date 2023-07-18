package org.zalando.baigan.service;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.baigan.model.Configuration;
import org.zalando.baigan.service.aws.S3FileLoader;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;

public class S3ConfigurationRepository extends AbstractConfigurationRepository {

    private static final Logger LOG = LoggerFactory.getLogger(S3ConfigurationRepository.class);

    /**
     * The default refresh interval which is 60 seconds
     */
    private static final long DEFAULT_REFRESH_INTERVAL = 60;

    private final S3FileLoader s3Loader;
    private final long refreshInterval;
    private final ScheduledThreadPoolExecutor executor;
    private volatile Map<String, Configuration> configurationsMap = ImmutableMap.of();

    /**
     * Provides a {@link ConfigurationRepository} that reads configurations from a JSON file stored in a S3 bucket.
     * It refreshes configurations using the default refresh interval (60 seconds)
     *
     * @deprecated use {@link S3ConfigurationRepository#S3ConfigurationRepository(S3FileLoader)} instead
     *
     * @param bucketName The name of the bucket
     * @param key        The object key, usually, the "full path" to the JSON file stored in the bucket
     */
    @Deprecated
    public S3ConfigurationRepository(@Nonnull final String bucketName, @Nonnull final String key) {
        this(bucketName, key, DEFAULT_REFRESH_INTERVAL);
    }

    /**
     * Provides a {@link ConfigurationRepository} that reads configurations from a JSON file stored in a S3 bucket.
     *
     * @deprecated use {@link S3ConfigurationRepository#S3ConfigurationRepository(S3FileLoader, long)} instead
     *
     * @param bucketName      The name of the bucket
     * @param key             The object key, usually, the "full path" to the JSON file stored in the bucket
     * @param refreshInterval The interval, in seconds, to refresh the configurations. A value of 0 disables refreshing
     *                        <p>
     * @see #S3ConfigurationRepository(String, String)
     */
    @Deprecated
    public S3ConfigurationRepository(@Nonnull final String bucketName, @Nonnull final String key, final long refreshInterval) {
        this(new S3FileLoader(bucketName, key), refreshInterval, new ScheduledThreadPoolExecutor(1));
    }

    /**
     * Provides a {@link ConfigurationRepository} that reads configurations from a JSON file stored in a S3 bucket.
     *
     * @deprecated Use {@link S3ConfigurationRepository#S3ConfigurationRepository(S3FileLoader, long, ScheduledThreadPoolExecutor)} instead
     *
     * @param bucketName      The name of the bucket
     * @param key             The object key, usually, the "full path" to the JSON file stored in the bucket
     * @param refreshInterval The interval, in seconds, to refresh the configurations. A value of 0 disables refreshing
     * @param executor        The executor to refresh the configurations in the specified interval
     *                        <p>
     * @see #S3ConfigurationRepository(String, String)
     */
    @Deprecated
    public S3ConfigurationRepository(@Nonnull final String bucketName, @Nonnull final String key,
                                     final long refreshInterval, final ScheduledThreadPoolExecutor executor) {
        this(new S3FileLoader(bucketName, key), refreshInterval, executor);
    }

    /**
     * Provides a {@link ConfigurationRepository} that uses the passed {@link S3FileLoader} to get the configuration data.
     *
     * @param s3Loader        The S3FileLoader that provides the configuration data.
     * @param refreshInterval The interval, in seconds, to refresh the configurations. A value of 0 disables refreshing
     * @param executor        The executor to refresh the configurations in the specified interval
     *                        <p>
     * @see #S3ConfigurationRepository(String, String)
     */
    public S3ConfigurationRepository(@Nonnull S3FileLoader s3Loader,
                                     final long refreshInterval, final ScheduledThreadPoolExecutor executor) {
        checkArgument(refreshInterval >= 0, "refreshInterval has to be >= 0");

        this.refreshInterval = refreshInterval;
        this.executor = executor;
        this.s3Loader = s3Loader;

        loadConfigurations();
        if (refreshInterval > 0) {
            setupRefresh();
        }
    }

    /**
     * Provides a {@link ConfigurationRepository} that uses the passed {@link S3FileLoader} to get the configuration data.
     * It uses a new {@link ScheduledThreadPoolExecutor} with corePoolSize = 1 to schedule the configuration update.
     *
     * @param s3Loader        The S3FileLoader that provides the configuration data.
     * @param refreshInterval The interval, in seconds, to refresh the configurations. A value of 0 disables refreshing
     *
     * @see #S3ConfigurationRepository(String, String)
     */
    public S3ConfigurationRepository(@Nonnull S3FileLoader s3Loader, final long refreshInterval) {
        this(s3Loader, refreshInterval, new ScheduledThreadPoolExecutor(1));
    }

    /**
     * Provides a {@link ConfigurationRepository} that uses the passed {@link S3FileLoader} to get the configuration data.
     * It uses a new {@link ScheduledThreadPoolExecutor} with corePoolSize = 1 to schedule the configuration update. The
     * default refresh interval is 60 seconds.
     *
     * @param s3Loader        The S3FileLoader that provides the configuration data.
     *
     * @see #S3ConfigurationRepository(String, String)
     */
    public S3ConfigurationRepository(@Nonnull S3FileLoader s3Loader) {
        this(s3Loader, DEFAULT_REFRESH_INTERVAL, new ScheduledThreadPoolExecutor(1));
    }

    protected void loadConfigurations() {
        final String configurationText = s3Loader.loadContent();
        final List<Configuration> configurations = getConfigurations(configurationText);
        final ImmutableMap.Builder<String, Configuration> builder = ImmutableMap.builder();
        for (final Configuration configuration : configurations) {
            builder.put(configuration.getAlias(), configuration);
        }
        configurationsMap = builder.build();
    }

    private void setupRefresh() {
        executor.scheduleAtFixedRate(
                () -> {
                    try {
                        loadConfigurations();
                    } catch (RuntimeException e) {
                        LOG.error("Failed to refresh configuration, keeping old state.", e);
                    }
                },
                this.refreshInterval,
                this.refreshInterval,
                TimeUnit.SECONDS
        );
    }

    @Nonnull
    @Override
    public Optional<Configuration> get(@Nonnull String key) {
        return Optional.ofNullable(configurationsMap.get(key));
    }

    @Override
    public void put(@Nonnull String key, @Nonnull String value) {
        throw new UnsupportedOperationException("The S3ConfigurationRepository doesn't allow any changes.");
    }
}
