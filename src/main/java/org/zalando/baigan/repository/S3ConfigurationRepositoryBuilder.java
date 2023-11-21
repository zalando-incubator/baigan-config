package org.zalando.baigan.repository;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import javax.annotation.Nonnull;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builder class for an S3ConfigurationRepository.
 * <p>
 * Must specify non-null values for
 * - {@link S3ConfigurationRepositoryBuilder#bucketName}
 * - {@link S3ConfigurationRepositoryBuilder#key}
 * <p>
 * The latter is typically set as the Spring bean named "baiganConfigClasses" provided by the library.
 */
public class S3ConfigurationRepositoryBuilder {

    private ScheduledThreadPoolExecutor executor;
    private AmazonS3 s3Client;
    private AWSKMS kmsClient;
    private long refreshIntervalInSeconds = 60;
    private String bucketName;
    private String key;

    S3ConfigurationRepositoryBuilder() {
    }

    /**
     * @param s3Client The S3 client to be used to fetch the configuration file.
     *                 If the S3 client is not specified explicitly, the builder
     *                 uses {@link AmazonS3ClientBuilder#defaultClient()}
     */
    public S3ConfigurationRepositoryBuilder s3Client(final AmazonS3 s3Client) {
        this.s3Client = s3Client;
        return this;
    }

    /**
     * @param kmsClient The KMS client to be used to decrypt the configuration file.
     *                  If the KMS client is not specified explicitly, the builder
     *                  uses {@link AWSKMSClientBuilder#defaultClient()}
     */
    public S3ConfigurationRepositoryBuilder kmsClient(final AWSKMS kmsClient) {
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
     */
    public S3ConfigurationRepositoryBuilder refreshIntervalInSeconds(final long refreshIntervalInSeconds) {
        this.refreshIntervalInSeconds = refreshIntervalInSeconds;
        return this;
    }

    /**
     * @param executor The {@link ScheduledThreadPoolExecutor} used to run the configuration refresh. If this is not
     *                 specified, a new {@link ScheduledThreadPoolExecutor} with a single thread is used.
     */
    public S3ConfigurationRepositoryBuilder executor(ScheduledThreadPoolExecutor executor) {
        this.executor = executor;
        return this;
    }

    public S3ConfigurationRepository build() {
        if (executor == null) {
            executor = new ScheduledThreadPoolExecutor(1);
        }
        if (s3Client == null) {
            s3Client = AmazonS3ClientBuilder.defaultClient();
        }
        if (kmsClient == null) {
            kmsClient = AWSKMSClientBuilder.defaultClient();
        }

        return new S3ConfigurationRepository(bucketName, key, refreshIntervalInSeconds, executor, s3Client, kmsClient);
    }
}
