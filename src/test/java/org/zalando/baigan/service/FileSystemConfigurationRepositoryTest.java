package org.zalando.baigan.service;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FileSystemConfigurationRepositoryTest {

    @Test
    public void testReadConfigurationsFromFile() {
        final ConfigurationRepository repo = new FileSystemConfigurationRepository(3, "src/test/resources/example.json");
        assertThat(repo.get("express.feature.toggle").get().getDefaultValue(), equalTo(false));
    }
}
