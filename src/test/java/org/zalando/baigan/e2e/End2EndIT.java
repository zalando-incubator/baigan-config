package org.zalando.baigan.e2e;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.zalando.baigan.fixture.SomeConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.zalando.baigan.e2e.TestContext.S3_CONFIG_BUCKET;
import static org.zalando.baigan.e2e.TestContext.S3_CONFIG_KEY;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestContext.class})
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
}
