package org.zalando.baigan.repository;

import jakarta.annotation.Nonnull;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.s3.S3Client;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builder class for an S3ConfigurationRepository.
 * <p>
 * Must specify non-null values for:
 * <ul>
 * <li>{@link S3ConfigurationRepositoryBuilder#bucketName(String)}
 * <li>{@link S3ConfigurationRepositoryBuilder#key(String)}
 * </ul>
 */
public class S3ConfigurationRepositoryBuilder {

    private ScheduledExecutorService executor;
    private S3Client s3Client;
    private KmsClient kmsClient;
    private Duration refreshInterval = Duration.ofMinutes(1);
    private String bucketName;
    private String key;
    private ObjectMapper objectMapper;
    private final ConfigurationParser configurationParser;

    public S3ConfigurationRepositoryBuilder(final ConfigurationParser configurationParser) {
        this.configurationParser = configurationParser;
    }

    /**
     * @param s3Client The S3 client to be used to fetch the configuration file.
     *                 If the S3 client is not specified explicitly, Baigan builds a default
     *                 client using {@link S3Client#builder()}.
     */
    public S3ConfigurationRepositoryBuilder s3Client(final S3Client s3Client) {
        this.s3Client = s3Client;
        return this;
    }

    /**
     * @param kmsClient The KMS client to be used to decrypt the configuration file.
     *                  If the KMS client is not specified explicitly, Baigan builds a default
     *                  client using {@link KmsClient#builder()}.
     */
    public S3ConfigurationRepositoryBuilder kmsClient(final KmsClient kmsClient) {
        this.kmsClient = kmsClient;
        return this;
    }

    /**
     * @param bucketName The name of the S3 bucket that holds the configuration file.
     */
    public S3ConfigurationRepositoryBuilder bucketName(@Nonnull final String bucketName) {
        this.bucketName = checkNotNull(bucketName, "bucketName must not be null");
        return this;
    }

    /**
     * @param key The S3 key pointing to the JSON configuration file in the specified bucket.
     */
    public S3ConfigurationRepositoryBuilder key(@Nonnull final String key) {
        this.key = checkNotNull(key, "key must not be null");
        return this;
    }

    /**
     * @param refreshIntervalInSeconds The number of seconds between the starts of subsequent runs to refresh
     *                                 the configuration
     * <p>
     * {@code @Deprecated} use {@link S3ConfigurationRepositoryBuilder#refreshInterval(Duration)} instead
     */
    @Deprecated
    public S3ConfigurationRepositoryBuilder refreshIntervalInSeconds(final long refreshIntervalInSeconds) {
        this.refreshInterval = Duration.ofSeconds(refreshIntervalInSeconds);
        return this;
    }

    /**
     * @param executor The {@link ScheduledThreadPoolExecutor} used to run the configuration refresh. If this is not
     *                 specified, a new {@link ScheduledThreadPoolExecutor} with a single thread is used.
     */
    public S3ConfigurationRepositoryBuilder executor(ScheduledExecutorService executor) {
        this.executor = executor;
        return this;
    }

    /**
     * @param objectMapper The {@link ObjectMapper} used to parse the configurations.
     */
    public S3ConfigurationRepositoryBuilder objectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    /**
     * @param refreshInterval The interval between the starts of subsequent runs to refresh the configuration.
     */
    public S3ConfigurationRepositoryBuilder refreshInterval(Duration refreshInterval) {
        this.refreshInterval = refreshInterval;
        return this;
    }

    public S3ConfigurationRepository build() {
        if (executor == null) {
            executor = new ScheduledThreadPoolExecutor(1);
        }
        if (s3Client == null) {
            s3Client = S3Client.builder().build();
        }
        if (kmsClient == null) {
            kmsClient = KmsClient.builder().build();
        }
        if (objectMapper != null) {
            configurationParser.setObjectMapper(objectMapper);
        }

        return new S3ConfigurationRepository(bucketName, key, refreshInterval, executor, s3Client, kmsClient, configurationParser);
    }
}
