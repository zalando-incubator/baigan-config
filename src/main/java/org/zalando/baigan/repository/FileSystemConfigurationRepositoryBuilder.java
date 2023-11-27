package org.zalando.baigan.repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import static java.util.Objects.requireNonNull;

/**
 * A builder to construct a {@link FileSystemConfigurationRepository}.
 * <p>
 * Requires that at least {@link FileSystemConfigurationRepositoryBuilder#fileName(String)} is specified.
 */
public class FileSystemConfigurationRepositoryBuilder {

    private String filePath;
    private long refreshIntervalInSeconds = 60L;
    private ObjectMapper objectMapper;
    private final ConfigurationParser configurationParser;

    FileSystemConfigurationRepositoryBuilder(final ConfigurationParser configurationParser) {
        this.configurationParser = configurationParser;
    }

    /**
     * @param filePath The path to the file that contains the configuration data.
     */
    public FileSystemConfigurationRepositoryBuilder fileName(String filePath) {
        this.filePath = filePath;
        return this;
    }

    /**
     * @param refreshIntervalInSeconds The number of seconds between the starts of subsequent runs to refresh
     *                                 the configuration.
     */
    public FileSystemConfigurationRepositoryBuilder refreshIntervalInSeconds(long refreshIntervalInSeconds) {
        this.refreshIntervalInSeconds = refreshIntervalInSeconds;
        return this;
    }

    /**
     * @param objectMapper The {@link ObjectMapper} used to parse the configurations.
     */
    public FileSystemConfigurationRepositoryBuilder objectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    public FileSystemConfigurationRepository build() {
        requireNonNull(filePath, "filePath must not be null");

        if (objectMapper != null) {
            configurationParser.setObjectMapper(objectMapper);
        }

        return new FileSystemConfigurationRepository(filePath, refreshIntervalInSeconds, configurationParser);
    }
}
