# Baigan configuration (Spring Boot module)

[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/baigan-config.svg)](https://maven-badges.herokuapp.com/maven-central/org.zalando/baigan-config)
[![Build Status](https://img.shields.io/travis/lukasniemeier-zalando/baigan-config/master.svg)](https://travis-ci.org/lukasniemeier-zalando/baigan-config)

Baigan configuration module providing a Spring Boot AutoConfiguration.

## Usage

Using this module, Baigan configuration is active out-of-the-box.
All you need to do is providing a `ConfigurationStore` bean:

```java
@Configuration
class StoreConfiguration {
    @Bean
    ConfigurationStore configurationStore() {
        return new NamespacedConfigurationStore(of(
                "FeatureAlpha", FileStores.builder().cached(ofMinutes(2)).onLocalFile(Path.of("example.json")).asJson(),
                "FeatureBeta", new CompositeConfigurationStore(
                        FileStores.builder().cached(ofMinutes(3)).on(s3("my-bucket", "config.yaml")).asYaml(),
                        new CustomInMemoryStore())
        ));
    }
}
```

## Dependencies

* Spring Boot 2.1+