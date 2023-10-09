package org.zalando.baigan.service;

import org.zalando.baigan.proxy.BaiganConfigClasses;

import static java.util.Objects.requireNonNull;

/**
 * A builder to construct a {@link FileSystemConfigurationRepository}.
 * <p>
 * Requires that at least {@link FileSystemConfigurationRepository::filePath} and
 * {@link BaiganConfigClasses} are specified.
 */
public class FileSystemConfigurationRepositoryBuilder {

    private String filePath;
    private long refreshIntervalInSeconds = 60L;
    private BaiganConfigClasses baiganConfigClasses;

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
     * @param baiganConfigClasses Contains the list of classes annotated with {@link org.zalando.baigan.annotation.BaiganConfig}.
     *                            This is typically set as the Spring bean named "baiganConfigClasses" provided by the library.
     */
    public FileSystemConfigurationRepositoryBuilder baiganConfigClasses(BaiganConfigClasses baiganConfigClasses) {
        this.baiganConfigClasses = baiganConfigClasses;
        return this;
    }

    public FileSystemConfigurationRepository build() {
        requireNonNull(filePath, "filePath must not be null");
        requireNonNull(baiganConfigClasses, "baiganConfigClasses must not be null");
        return new FileSystemConfigurationRepository(filePath, refreshIntervalInSeconds, baiganConfigClasses);
    }
}
