# Baigan configuration

[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/baigan-config.svg)](https://maven-badges.herokuapp.com/maven-central/org.zalando/baigan-config)
[![Build Status](https://img.shields.io/travis/lukasniemeier-zalando/baigan-config/master.svg)](https://travis-ci.org/lukasniemeier-zalando/baigan-config)

Baigan configuration is an easy to use framework for Java applications allowing you to store and fetch runtime configuration.

Its simple but extensible interfaces allow for powerful integrations.

* fetch your configuration from arbitrary sources serving any format (e.g. local files, S3, ...)
* integrate with the [Spring Framework](https://spring.io/) enabling access to your configuration by simply annotating an interface.
* integrate with Baigan configuration in seconds, via the provided [Spring Boot](https://spring.io/projects/spring-boot) Auto-configuration library

## Usage

Usage of Baigan configuration with the provided Spring Boot integration is as easy as:

```
@BaiganConfiguration
interface ServiceConfiguration {
    String serviceName();
}

@Component
class Service {

    @Autowired
    private ServiceConfiguration configuration;

    public String whoAmI() {
        return configuration.serviceName();
    }
}

```

Serving of such runtime configuration may be done via a simple configuration YAML:

```
ServiceConfiguration:
  serviceName:
    value: My Example Service
    description: Defining the service's name.
```

See the [Spring Boot](spring-boot-autoconfigure) and [Spring](spring) module for further information.

## Concepts

At its core, Baigan configuration defines two entities.

- `Configuration` represents the runtime configuration. It's accessible by its `key` and holds a corresponding `value`. 
In order to facilitate documentation, a configuration object must have a `description`.
- `ConfigurationStore` represents a collection of configuration objects. 
A configuration store is organizing the held configuration objects in *namespaces*.

Implementations of `ConfigurationStore` may serve configuration objects from arbitrary sources, e.g. memory, local files, S3 buckets, databases.
Looking up a `Configuration` by its key, may involve traversing a *chain* of `ConfigurationStores` (allowing you to define *fallback* stores) or
is routed by the *namespace* (allowing you to define different sources per namespace).

## Integrations

Baigan configuration comes with a set of powerful integrations.

* The [Spring](spring) module enables accessing configuration via annotated interfaces in Spring applications.
* The [Spring Boot](spring-boot-autoconfigure) module simplifies configuration in Spring applications.
* The [File](file) module provides a `ConfigurationStore` implementation serving from JSON and YAML files.
* The [S3](s3) module allows for fetching configuration files from S3 buckets.

## Development

To build the project run

```bash
$ ./gradlew clean check
```
