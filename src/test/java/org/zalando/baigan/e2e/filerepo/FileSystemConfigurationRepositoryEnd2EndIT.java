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
import org.zalando.baigan.e2e.configs.SomeConfigObject;
import org.zalando.baigan.e2e.configs.SomeConfiguration;
import org.zalando.baigan.service.FileSystemConfigurationRepository;
import org.zalando.baigan.service.RepositoryFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FileSystemConfigurationRepositoryEnd2EndIT.RepoConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FileSystemConfigurationRepositoryEnd2EndIT {

    @Autowired
    private SomeConfiguration someConfiguration;

    @Autowired
    private Path configFile;

    @Test
    public void givenAConfigurationFile_whenConfigurationIsChanged_thenConfigurationBeanReturnsNewConfigAfterRefreshTime() throws InterruptedException, IOException {
        assertThat(someConfiguration.isThisTrue(), nullValue());
        assertThat(someConfiguration.someValue(), nullValue());
        assertThat(someConfiguration.someConfig(), nullValue());

        Files.writeString(configFile, "[{\"alias\": \"some.configuration.some.value\", \"defaultValue\": \"some value\"}]");
        Thread.sleep(1100);
        assertThat(someConfiguration.isThisTrue(), nullValue());
        assertThat(someConfiguration.someValue(), equalTo("some value"));
        assertThat(someConfiguration.someConfig(), nullValue());

        Files.writeString(configFile, "[{ \"alias\": \"some.non.existing.config\", \"defaultValue\": \"an irrelevant value\"}," +
                "{ \"alias\": \"some.configuration.is.this.true\", \"defaultValue\": true}, " +
                "{ \"alias\": \"some.configuration.some.value\", \"defaultValue\": \"some value\"}, " +
                "{ \"alias\": \"some.configuration.some.config\", \"defaultValue\": {\"config_key\":\"a value\"}}, " +
                "{ \"alias\": \"some.configuration.config.list\", \"defaultValue\": [\"A\",\"B\"]}]"
        );
        Thread.sleep(1100);
        assertThat(someConfiguration.isThisTrue(), equalTo(true));
        assertThat(someConfiguration.someValue(), equalTo("some value"));
        assertThat(someConfiguration.someConfig(), equalTo(new SomeConfigObject("a value")));
        assertThat(someConfiguration.configList(), equalTo(List.of("A", "B")));
    }

    @Test
    public void givenAConfigurationFile_whenTheFileIsUpdatedWithInvalidConfig_thenTheConfigurationIsNotUpdated() throws InterruptedException, IOException {
        Files.writeString(configFile, "[{ \"alias\": \"some.non.existing.config\", \"defaultValue\": \"an irrelevant value\"}," +
                "{ \"alias\": \"some.configuration.is.this.true\", \"defaultValue\": true}, " +
                "{ \"alias\": \"some.configuration.some.value\", \"defaultValue\": \"some value\"}]"
        );
        Thread.sleep(1100);
        assertThat(someConfiguration.isThisTrue(), equalTo(true));
        assertThat(someConfiguration.someValue(), equalTo("some value"));

        Files.writeString(configFile, "{invalid: \"configuration]");
        Thread.sleep(1100);
        assertThat(someConfiguration.isThisTrue(), equalTo(true));
        assertThat(someConfiguration.someValue(), equalTo("some value"));
    }

    @Test
    public void givenAConfigurationFile_whenConfigurationTypeIsGeneric_thenDeserializesProperly() throws IOException, InterruptedException {
        Files.writeString(configFile, "[{\"alias\": \"some.configuration.top.level.generics\",\"defaultValue\": {" +
                "\"a8a23682-1623-450b-8817-50c98827ea4e\": [{\"config_key\":\"A\"}]," +
                "\"76ced443-6555-4748-a22e-8700f3864e59\": [{\"config_key\":\"B\"}]}" +
                "}]");
        Thread.sleep(1100);
        assertThat(someConfiguration.topLevelGenerics(), equalTo(Map.of(
                UUID.fromString("a8a23682-1623-450b-8817-50c98827ea4e"), List.of(new SomeConfigObject("A")),
                UUID.fromString("76ced443-6555-4748-a22e-8700f3864e59"), List.of(new SomeConfigObject("B"))
        )));
    }

    @ConfigurationServiceScan(basePackages = "org.zalando.baigan.e2e.configs")
    @Testcontainers
    @ComponentScan(basePackageClasses = {BaiganSpringContext.class})
    static class RepoConfig {

        @Bean
        FileSystemConfigurationRepository configurationRepository(Path configFile, RepositoryFactory repositoryFactory) {
            return repositoryFactory.fileSystemConfigurationRepository()
                    .fileName(configFile.toString())
                    .refreshIntervalInSeconds(1)
                    .build();
        }

        @Bean("configFile")
        Path configFile() {
            try {
                final Path configFile = Files.createTempFile("config", "json");
                Files.writeString(configFile, "[]");
                return configFile;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Bean(name = "baiganObjectMapper")
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
