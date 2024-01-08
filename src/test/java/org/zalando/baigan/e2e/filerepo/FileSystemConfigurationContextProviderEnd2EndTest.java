package org.zalando.baigan.e2e.filerepo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.zalando.baigan.BaiganSpringContext;
import org.zalando.baigan.annotation.ConfigurationServiceScan;
import org.zalando.baigan.e2e.configs.SomeConfiguration;
import org.zalando.baigan.repository.FileSystemConfigurationRepository;
import org.zalando.baigan.repository.RepositoryFactory;

import java.time.Duration;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FileSystemConfigurationContextProviderEnd2EndTest.RepoConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FileSystemConfigurationContextProviderEnd2EndTest {

    @Autowired
    private SomeConfiguration someConfiguration;

    private static final Duration CONFIG_REFRESH_INTERVAL = Duration.ofMillis(100);

    @Test
    public void testConfigurationsWithContext() {
        assertThat(someConfiguration.toggleFlag(new CustomContextProvider("1")), equalTo(true));
        assertThat(someConfiguration.toggleFlag(new CustomContextProvider("2")), equalTo(false));
        assertThat(someConfiguration.toggleFlag(null), equalTo(false));
    }

    @ConfigurationServiceScan(basePackageClasses = SomeConfiguration.class)
    @Testcontainers
    @ComponentScan(basePackageClasses = {BaiganSpringContext.class})
    static class RepoConfig {

        @Bean
        FileSystemConfigurationRepository configurationRepository(RepositoryFactory repositoryFactory) {
            return repositoryFactory.fileSystemConfigurationRepository()
                    .fileName(FileSystemConfigurationContextProviderEnd2EndTest.class.getClassLoader().getResource("test-config.json").getPath())
                    .refreshInterval(CONFIG_REFRESH_INTERVAL)
                    .objectMapper(new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false))
                    .build();
        }
    }
}
