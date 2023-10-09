package org.zalando.baigan.e2e;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.zalando.baigan.BaiganSpringContext;
import org.zalando.baigan.annotation.ConfigurationServiceScan;
import org.zalando.baigan.fixture.SomeConfiguration;
import org.zalando.baigan.service.ConfigurationRepository;
import org.zalando.baigan.service.aws.S3ConfigurationRepositoryBuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.KMS;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static org.zalando.baigan.e2e.End2EndIT.RepoConfig.S3_CONFIG_BUCKET;
import static org.zalando.baigan.e2e.End2EndIT.RepoConfig.S3_CONFIG_KEY;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {End2EndIT.RepoConfig.class})
public class End2EndIT {

    @Autowired
    private AmazonS3 s3;

    @Autowired
    private SomeConfiguration someConfiguration;

    @Test
    public void givenS3Configuration_whenConfigurationIsChangedOnS3_thenConfigurationBeanReturnsNewConfigAfterRefreshTime() throws InterruptedException {
        assertThat(someConfiguration.someValue(), nullValue());
        s3.putObject(S3_CONFIG_BUCKET, S3_CONFIG_KEY, "[{ \"alias\": \"some.configuration.some.value\", \"defaultValue\": \"a value\"}]");
        Thread.sleep(1100);
        assertThat(someConfiguration.someValue(), equalTo("a value"));
    }

    @ComponentScan(basePackageClasses = BaiganSpringContext.class)
    @ConfigurationServiceScan(basePackages = "org.zalando.baigan.fixture")
    @Testcontainers
    static class RepoConfig {

        public static final String S3_CONFIG_BUCKET = "some-bucket";
        public static final String S3_CONFIG_KEY = "some-key";

        @Container
        private static final LocalStackContainer localstack = new LocalStackContainer(
                DockerImageName.parse("localstack/localstack:2.1.0")
        ).withServices(S3, KMS).withEnv("DEFAULT_REGION", Regions.EU_CENTRAL_1.getName());

        @Bean
        ConfigurationRepository configurationRepository(AmazonS3 amazonS3, AWSKMS kms) {
            amazonS3.putObject(S3_CONFIG_BUCKET, S3_CONFIG_KEY, "[]");

            return new S3ConfigurationRepositoryBuilder()
                    .bucketName(S3_CONFIG_BUCKET)
                    .key(S3_CONFIG_KEY)
                    .s3Client(amazonS3)
                    .kmsClient(kms)
                    .refreshIntervalInSeconds(1)
                    .build();
        }

        @Bean
        AWSKMS kms() {
            localstack.start();
            return AWSKMSClientBuilder.standard().withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(
                            localstack.getEndpointOverride(KMS).toString(), localstack.getRegion()
                    )
            ).build();
        }

        @Bean
        AmazonS3 amazonS3() {
            localstack.start();
            AmazonS3 s3 = AmazonS3ClientBuilder.standard().withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(
                            localstack.getEndpointOverride(S3).toString(), localstack.getRegion()
                    )
            ).withCredentials(
                    new AWSStaticCredentialsProvider(
                            new BasicAWSCredentials(localstack.getAccessKey(), localstack.getSecretKey())
                    )
            ).build();

            try {
                s3.createBucket(new CreateBucketRequest(S3_CONFIG_BUCKET, localstack.getRegion()));
            } catch (AmazonS3Exception e) {
                if (!e.getErrorCode().equals("BucketAlreadyOwnedByYou")) {
                    throw e;
                }
            }

            return s3;
        }
    }
}
