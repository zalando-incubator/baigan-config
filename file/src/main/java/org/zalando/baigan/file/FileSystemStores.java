package org.zalando.baigan.file;

import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.zalando.baigan.ConfigurationStore;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;

public final class FileSystemStores {

    public static StoreBuilder builder() {
        return duration -> supplier -> mapper -> new FileBasedConfigurationStore(
                new CachingConfigurationFileSupplier(
                        duration,
                        new ConfigurationFileSupplier(
                                supplier,
                                mapper)));
    }

    public interface StoreBuilder {

        SupplierBuilder cached(final Duration duration);

        interface SupplierBuilder {
            FormatBuilder on(final Supplier<String> supplier);

            default FormatBuilder onLocalFile(final Path path) {
                return on(new LocalFileSupplier(path));
            }

            interface FormatBuilder {
                ConfigurationStore asFormat(final Function<String, ConfigurationFile> mapper);

                default ConfigurationStore asJson() {
                    return asFormat(new JacksonConfigurationFileMapper(new ObjectMapper(new MappingJsonFactory())));
                }

                default ConfigurationStore asYaml() {
                    return asFormat(new JacksonConfigurationFileMapper(new ObjectMapper(new YAMLFactory())));
                }
            }
        }
    }

    private FileSystemStores() {
        // utility class
    }
}
