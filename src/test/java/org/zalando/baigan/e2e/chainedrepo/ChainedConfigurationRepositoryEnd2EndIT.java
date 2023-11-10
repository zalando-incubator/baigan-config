package org.zalando.baigan.e2e.chainedrepo;

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
import org.zalando.baigan.e2e.configs.SomeConfigObject;
import org.zalando.baigan.e2e.configs.SomeConfiguration;
import org.zalando.baigan.service.ConfigurationRepository;
import org.zalando.baigan.service.RepositoryFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ChainedConfigurationRepositoryEnd2EndIT.RepoConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ChainedConfigurationRepositoryEnd2EndIT {

    @Autowired
    private SomeConfiguration someConfiguration;

    private static final Path CONFIG_FILE_1 = createConfigFile("config1");
    private static final Path CONFIG_FILE_2 = createConfigFile("config2");

    @Test
    public void givenAChainedRepository_whenConfigIsPresentInAnyRepository_thenTheConfigHasTheCorrespondingValue() throws InterruptedException, IOException {
        assertThat(someConfiguration.isThisTrue(), nullValue());
        assertThat(someConfiguration.someValue(), nullValue());
        assertThat(someConfiguration.someConfig(), nullValue());
        assertThat(someConfiguration.configList(), nullValue());
        assertThat(someConfiguration.topLevelGenerics(), nullValue());

        Files.writeString(CONFIG_FILE_1, "[{\"alias\": \"some.configuration.some.value\", \"defaultValue\": \"some value\"}]");
        Files.writeString(CONFIG_FILE_2, "[{ \"alias\": \"some.configuration.some.config\", \"defaultValue\": {\"config_key\":\"a value\"}}]");
        Thread.sleep(1100);

        assertThat(someConfiguration.someValue(), equalTo("some value"));
        assertThat(someConfiguration.someConfig(), equalTo(new SomeConfigObject("a value")));
        assertThat(someConfiguration.isThisTrue(), nullValue());
        assertThat(someConfiguration.configList(), nullValue());
        assertThat(someConfiguration.topLevelGenerics(), nullValue());
    }

    private static Path createConfigFile(String fileName) {
        try {
            final Path configFile = Files.createTempFile(fileName, "json");
            Files.writeString(configFile, "[]");
            return configFile;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ConfigurationServiceScan(basePackages = "org.zalando.baigan.e2e.configs")
    @Testcontainers
    @ComponentScan(basePackageClasses = {BaiganSpringContext.class})
    static class RepoConfig {

        @Bean
        ConfigurationRepository configurationRepository(RepositoryFactory repositoryFactory) {
            ConfigurationRepository repo1 = repositoryFactory.fileSystemConfigurationRepository()
                    .fileName(CONFIG_FILE_1.toString())
                    .refreshIntervalInSeconds(1)
                    .build();

            ConfigurationRepository repo2 = repositoryFactory.fileSystemConfigurationRepository()
                    .fileName(CONFIG_FILE_2.toString())
                    .refreshIntervalInSeconds(1)
                    .build();

            return repositoryFactory.chainedConfigurationRepository(List.of(repo1, repo2));
        }
    }
}
