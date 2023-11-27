package org.zalando.baigan.repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;

import static java.util.Objects.requireNonNull;

/**
 * A builder to construct a {@link FileSystemConfigurationRepository}.
 * <p>
 * Requires that at least {@link FileSystemConfigurationRepositoryBuilder#fileName(String)} is specified.
 */
public class FileSystemConfigurationRepositoryBuilder {

    private String filePath;
    private Duration refreshInterval = Duration.ofMinutes(1);
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
     * <p>
     * {@code @Deprecated} use {@link S3ConfigurationRepositoryBuilder#refreshInterval(Duration)} instead
     */
    public FileSystemConfigurationRepositoryBuilder refreshIntervalInSeconds(long refreshIntervalInSeconds) {
        this.refreshInterval = Duration.ofSeconds(refreshIntervalInSeconds);
        return this;
    }

    /**
     * @param objectMapper The {@link ObjectMapper} used to parse the configurations.
     */
    public FileSystemConfigurationRepositoryBuilder objectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    /**
     * @param refreshInterval The interval between the starts of subsequent runs to refresh the configuration.
     */
    public FileSystemConfigurationRepositoryBuilder refreshInterval(Duration refreshInterval) {
        this.refreshInterval = refreshInterval;
        return this;
    }

    public FileSystemConfigurationRepository build() {
        requireNonNull(filePath, "filePath must not be null");

        if (objectMapper != null) {
            configurationParser.setObjectMapper(objectMapper);
        }

        return new FileSystemConfigurationRepository(filePath, refreshInterval, configurationParser);
    }
}
