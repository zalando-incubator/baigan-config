package org.zalando.baigan.e2e.etcdrepo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.zalando.baigan.BaiganSpringContext;
import org.zalando.baigan.annotation.ConfigurationServiceScan;
import org.zalando.baigan.e2e.configs.SomeConfiguration;
import org.zalando.baigan.etcd.service.EtcdClient;
import org.zalando.baigan.service.EtcdConfigurationRepository;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EtcdConfigurationRepositoryEnd2EndIT.RepoConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EtcdConfigurationRepositoryEnd2EndIT {

    @Autowired
    private SomeConfiguration someConfiguration;
    @Autowired
    private GenericContainer<?> etcd;

    private static final HttpClient client = HttpClient.newHttpClient();

    @Test
    public void givenEtcdConfiguration_whenKeyIsSetInEtcd_shouldProvideValueAsConfig() {
        assertThat(someConfiguration.someValue(), nullValue());
        setKeyInEtcd("some.configuration.some.value", "{\"alias\": \"some.configuration.some.value\", \"defaultValue\": \"some value\"}");
        setKeyInEtcd("some.configuration.config.list", "{ \"alias\": \"some.configuration.config.list\", \"defaultValue\": [\"A\",\"B\"]}");
        assertThat(someConfiguration.someValue(), equalTo("some value"));
        assertThat(someConfiguration.configList(), equalTo(List.of("A", "B")));
    }

    private void setKeyInEtcd(final String key, final String value) {
        final String url = "http://localhost:" + etcd.getMappedPort(2379) + "/v2/keys/" + key;
        final String jsonPayload = "value=" + value;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
        try {
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode(), lessThan(300));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @ConfigurationServiceScan(basePackages = "org.zalando.baigan.e2e.configs")
    @Testcontainers
    @ComponentScan(basePackageClasses = {BaiganSpringContext.class})
    static class RepoConfig {

        @Bean
        EtcdConfigurationRepository configurationRepository(GenericContainer<?> etcd) {
            return new EtcdConfigurationRepository(new EtcdClient("http://localhost:" + etcd.getMappedPort(2379)));
        }

        @Container
        @SuppressWarnings("resource")
        private static final GenericContainer<?> etcd = new GenericContainer<>(
                DockerImageName.parse("quay.io/coreos/etcd:v2.3.8")
        ).withExposedPorts(2379, 2380, 4001)
                .withCommand(
                        "-name", "node1",
                        "-advertise-client-urls", "http://192.168.12.50:2379,http://192.168.12.50:4001",
                        "-listen-client-urls", "http://0.0.0.0:2379,http://0.0.0.0:4001",
                        "-initial-advertise-peer-urls", "http://192.168.12.50:2380",
                        "-listen-peer-urls", "http://0.0.0.0:2380",
                        "-initial-cluster-token", "etcd-cluster-1",
                        "-initial-cluster", "node1=http://192.168.12.50:2380",
                        "-initial-cluster-state", "new"
                );

        @Bean
        public GenericContainer<?> etcd() {
            etcd.start();
            return etcd;
        }
    }
}
