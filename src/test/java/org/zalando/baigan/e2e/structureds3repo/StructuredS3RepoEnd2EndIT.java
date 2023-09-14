package org.zalando.baigan.e2e.structureds3repo;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.zalando.baigan.annotation.ConfigurationServiceScan;
import org.zalando.baigan.e2e.TestContext;
import org.zalando.baigan.proxy.BaiganConfigClasses;
import org.zalando.baigan.service.aws.S3ConfigurationRepository;
import org.zalando.baigan.service.aws.S3ConfigurationRepositoryBuilder;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.zalando.baigan.e2e.TestContext.S3_CONFIG_BUCKET;
import static org.zalando.baigan.e2e.TestContext.S3_CONFIG_KEY;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestContext.class, StructuredS3RepoEnd2EndIT.RepoConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StructuredS3RepoEnd2EndIT {

    @Autowired
    private AmazonS3 s3;

    @Autowired
    private SomeComplexConfiguration someConfiguration;

    @Autowired
    private ScheduledThreadPoolExecutor executor;

    @Test
    public void givenS3Configuration_whenConfigurationIsChangedOnS3_thenConfigurationBeanReturnsNewConfigAfterRefreshTime() throws InterruptedException {
        assertThat(someConfiguration.someConfig(), nullValue());
        s3.putObject(
                S3_CONFIG_BUCKET,
                S3_CONFIG_KEY,
                "[{ \"alias\": \"some.complex.configuration.some.config\", \"defaultValue\": {\"config_key\":\"a value\"}}," +
                        "{ \"alias\": \"some.complex.configuration.some.other.config\", \"defaultValue\": {\"other_config_key\":\"other value\"}}]");
        Thread.sleep(1100);
        assertThat(someConfiguration.someConfig(), equalTo(new SomeConfigObject("a value")));
    }

    @AfterAll
    public void cleanup() {
        executor.shutdownNow();
    }

    @ConfigurationServiceScan(basePackages = "org.zalando.baigan.e2e.structureds3repo")
    static class RepoConfig {

        @Bean(destroyMethod = "shutdownNow")
        ScheduledThreadPoolExecutor baiganRefresherPoolExecutor(){
            return new ScheduledThreadPoolExecutor(1);
        }

        @Bean
        S3ConfigurationRepository configurationRepository(AmazonS3 amazonS3, AWSKMS kms, BaiganConfigClasses baiganConfigClasses, ScheduledThreadPoolExecutor executorService) {
            amazonS3.putObject(TestContext.S3_CONFIG_BUCKET, TestContext.S3_CONFIG_KEY, "[]");
            return new S3ConfigurationRepositoryBuilder()
                    .bucketName(TestContext.S3_CONFIG_BUCKET)
                    .key(TestContext.S3_CONFIG_KEY)
                    .s3Client(amazonS3)
                    .kmsClient(kms)
                    .refreshIntervalInSeconds(1)
                    .executor(executorService)
                    .baiganConfigClasses(baiganConfigClasses)
                    .build();
        }
    }
}
