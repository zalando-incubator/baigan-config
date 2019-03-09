package org.zalando.baigan.file;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.function.Supplier;

import static java.lang.String.join;
import static java.nio.file.Files.readAllLines;

final class LocalConfigurationFileSupplier implements Supplier<String> {

    private final Path path;

    LocalConfigurationFileSupplier(final Path path) {
        this.path = path;
    }

    @Override
    public String get() {
        try {
            return join("\n", readAllLines(path));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
