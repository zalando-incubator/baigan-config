package org.zalando.baigan.service.aws;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import static com.google.common.base.Preconditions.checkNotNull;

public class S3ConfigurationRepositoryBuilder {

    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
    private AWSKMS kmsClient = AWSKMSClientBuilder.defaultClient();
    private long refreshIntervalInSeconds = 60;
    private String bucketName;
    private String key;

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
    public S3ConfigurationRepositoryBuilder bucketName(final String bucketName) {
        this.bucketName = checkNotNull(bucketName, "bucketName must not be null");
        return this;
    }

    /**
     * @param key The S3 key pointing to the JSON configuration file in the specified bucket.
     */
    public S3ConfigurationRepositoryBuilder key(final String key) {
        this.key = checkNotNull(key, "key must not be null");
        return this;
    }

    /**
     * @param refreshIntervalInSeconds The number of seconds between the start of a run to refresh the configuration.
     */
    public S3ConfigurationRepositoryBuilder refreshIntervalInSeconds(final long refreshIntervalInSeconds) {
        this.refreshIntervalInSeconds = refreshIntervalInSeconds;
        return this;
    }

    /**
     * @param executor The {@link ScheduledThreadPoolExecutor} used to run the configuration refresh.
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
