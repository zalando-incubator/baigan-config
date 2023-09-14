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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.zalando.baigan.BaiganSpringContext;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.KMS;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@ComponentScan(basePackageClasses = {BaiganSpringContext.class})
@Testcontainers
public class TestContext {

    public static final String S3_CONFIG_BUCKET = "some-bucket";
    public static final String S3_CONFIG_KEY = "some-key";

    @Container
    private static final LocalStackContainer localstack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:2.1.0")
        ).withServices(S3, KMS).withEnv("DEFAULT_REGION", Regions.EU_CENTRAL_1.getName());

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
