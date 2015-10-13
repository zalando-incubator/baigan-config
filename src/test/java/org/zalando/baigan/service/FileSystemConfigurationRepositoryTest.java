package org.zalando.baigan.service;

import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class FileSystemConfigurationRepositoryTest {

    @Test
    public void testReadConfigurationsFromFile() {
        final ConfigurationRespository repo = new FileSytemConfigurationRespository(
                3, "example.json");
        assertThat(repo.getConfig("express.feature.toggle").get()
                .getDefaultValue(), Matchers.equalTo(false));
    }
}
