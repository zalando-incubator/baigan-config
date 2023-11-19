package org.zalando.baigan.repository;

import static java.util.Objects.requireNonNull;

/**
 * A builder to construct a {@link FileSystemConfigurationRepository}.
 * <p>
 * Requires that at least {@link FileSystemConfigurationRepository::filePath} is specified.
 */
public class FileSystemConfigurationRepositoryBuilder {

    private String filePath;
    private long refreshIntervalInSeconds = 60L;
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

    public FileSystemConfigurationRepository build() {
        requireNonNull(filePath, "filePath must not be null");
        return new FileSystemConfigurationRepository(filePath, refreshIntervalInSeconds, configurationParser);
    }
}
