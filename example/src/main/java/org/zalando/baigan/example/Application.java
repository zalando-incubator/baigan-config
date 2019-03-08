package org.zalando.baigan.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.zalando.baigan.ConfigurationStore;
import org.zalando.baigan.file.FileStores;
import java.nio.file.Path;
import java.time.Duration;

@SpringBootApplication
class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
        final ConfigurationStore store = FileStores.builder()
                .cached(Duration.ofMinutes(5))
                .onLocalFile(Path.of("configuration.yaml"))
                .asYaml();

    }

}
