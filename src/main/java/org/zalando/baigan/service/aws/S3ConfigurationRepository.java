package org.zalando.baigan.service.aws;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.baigan.model.Configuration;
import org.zalando.baigan.proxy.BaiganConfigClasses;
import org.zalando.baigan.service.AbstractConfigurationRepository;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class S3ConfigurationRepository extends AbstractConfigurationRepository {

    private static final Logger LOG = LoggerFactory.getLogger(S3ConfigurationRepository.class);

    private final S3FileLoader s3Loader;
    private final long refreshInterval;
    private final ScheduledThreadPoolExecutor executor;
    private volatile Map<String, Configuration<?>> configurationsMap = ImmutableMap.of();

    S3ConfigurationRepository(@Nonnull final String bucketName, @Nonnull final String key,
                              final long refreshInterval, final ScheduledThreadPoolExecutor executor,
                              final AmazonS3 s3Client, final AWSKMS kmsClient, final BaiganConfigClasses baiganConfigClasses,
                              final ObjectMapper objectMapper) {
        super(baiganConfigClasses, objectMapper);
        checkNotNull(bucketName, "bucketName is required");
        checkNotNull(key, "key is required");
        checkArgument(refreshInterval >= 0, "refreshInterval has to be >= 0");
        checkNotNull(executor, "executor is required");
        checkNotNull(s3Client, "s3Client is required");
        checkNotNull(kmsClient, "kmsClient is required");

        this.refreshInterval = refreshInterval;
        this.executor = executor;
        this.s3Loader = new S3FileLoader(bucketName, key, s3Client, kmsClient);

        loadConfigurations();
        if (refreshInterval > 0) {
            setupRefresh();
        }
    }

    protected void loadConfigurations() {
        final String configurationText = s3Loader.loadContent();
        final List<Configuration<?>> configurations = getConfigurations(configurationText);
        final ImmutableMap.Builder<String, Configuration<?>> builder = ImmutableMap.builder();
        for (final Configuration<?> configuration : configurations) {
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
