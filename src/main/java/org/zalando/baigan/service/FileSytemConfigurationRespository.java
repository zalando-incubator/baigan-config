package org.zalando.baigan.service;

/**
 * This class had typos in its name and is being kept for backwards compatibility only
 *
 * @deprecated use {@link FileSystemConfigurationRepository} instead.
 */
@Deprecated
public final class FileSytemConfigurationRespository extends FileSystemConfigurationRepository {

    @Deprecated
    public FileSytemConfigurationRespository(long refreshIntervalInMinutes, final String fileName) {
        super(refreshIntervalInMinutes, fileName);
    }
}
