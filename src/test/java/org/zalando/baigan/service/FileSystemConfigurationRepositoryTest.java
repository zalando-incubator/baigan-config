package org.zalando.baigan.service;

import org.junit.jupiter.api.Test;
import org.zalando.baigan.proxy.BaiganConfigClasses;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FileSystemConfigurationRepositoryTest {

    @Test
    public void testReadConfigurationsFromFile() {
        final Map<String, Class<?>> configTypesByKey = Map.of(
                "express.feature.toggle", Boolean.class,
                "express.feature.service.url", String.class,
                "pession.sync.feature.toggle", Boolean.class
        );
        final ConfigurationRepository repo = new FileSystemConfigurationRepository("src/test/resources/example.json", 180, new BaiganConfigClasses(configTypesByKey));
        assertThat(repo.get("express.feature.toggle").get().getDefaultValue(), equalTo(false));
    }
}
