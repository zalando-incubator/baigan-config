package org.zalando.baigan.e2e.simples3repo;

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

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestContext.class, SimpleS3RepoEnd2EndIT.RepoConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SimpleS3RepoEnd2EndIT {

    @Autowired
    private AmazonS3 s3;

    @Autowired
    private SomePlainConfiguration someConfiguration;

    @Autowired
    private ScheduledThreadPoolExecutor executor;

    @Test
    public void givenS3Configuration_whenConfigurationIsChangedOnS3_thenConfigurationBeanReturnsNewConfigAfterRefreshTime() throws InterruptedException {
        assertThat(someConfiguration.someValue(), nullValue());
        assertThat(someConfiguration.isThisTrue(), nullValue());
        s3.putObject(TestContext.S3_CONFIG_BUCKET, TestContext.S3_CONFIG_KEY, "[{ \"alias\": \"some.plain.configuration.some.value\", \"defaultValue\": \"a value\"}]");
        Thread.sleep(1100);
        assertThat(someConfiguration.someValue(), equalTo("a value"));
        assertThat(someConfiguration.isThisTrue(), nullValue());
        s3.putObject(
                TestContext.S3_CONFIG_BUCKET,
                TestContext.S3_CONFIG_KEY,
                "[{ \"alias\": \"some.plain.configuration.is.this.true\", \"defaultValue\": true}, " +
                        "{ \"alias\": \"some.plain.configuration.some.value\", \"defaultValue\": \"a value\"}]"
        );
        Thread.sleep(1100);
        assertThat(someConfiguration.someValue(), equalTo("a value"));
        assertThat(someConfiguration.isThisTrue(), equalTo(true));
    }

    @AfterAll
    public void cleanup() {
        executor.shutdownNow();
    }

    @ConfigurationServiceScan(basePackages = "org.zalando.baigan.e2e.simples3repo")
    static class RepoConfig {

        @Bean(destroyMethod = "shutdownNow")
        ScheduledThreadPoolExecutor baiganRefresherPoolExecutor(){
            return new ScheduledThreadPoolExecutor(1);
        }

        @Bean
        S3ConfigurationRepository configurationRepository(AmazonS3 amazonS3, AWSKMS kms, BaiganConfigClasses baiganConfigClasses, ScheduledThreadPoolExecutor executor) {
            amazonS3.putObject(TestContext.S3_CONFIG_BUCKET, TestContext.S3_CONFIG_KEY, "[]");

            return new S3ConfigurationRepositoryBuilder()
                    .bucketName(TestContext.S3_CONFIG_BUCKET)
                    .key(TestContext.S3_CONFIG_KEY)
                    .s3Client(amazonS3)
                    .kmsClient(kms)
                    .refreshIntervalInSeconds(1)
                    .executor(executor)
                    .baiganConfigClasses(baiganConfigClasses)
                    .build();
        }

    }
}
