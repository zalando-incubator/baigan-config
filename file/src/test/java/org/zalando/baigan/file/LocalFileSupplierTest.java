package org.zalando.baigan.file;

import org.junit.jupiter.api.Test;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static java.lang.ClassLoader.getSystemResource;

class LocalFileSupplierTest {

    private final LocalFileSupplier unit = unit();

    @Test
    void keepsNewlines() {
        final String content = unit.get();
        assertNotEquals(0, content.split("\n").length);
    }

    @Test
    void readsFile() {
        final String content = unit.get();
        assertTrue(content.contains("42"));
    }

    private LocalFileSupplier unit() {
        try {
            return new LocalFileSupplier(Paths.get(getSystemResource("example.json").toURI()));
        } catch (final URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}