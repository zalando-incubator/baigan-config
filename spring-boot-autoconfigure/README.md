# Baigan configuration (Spring Boot module)

[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/baigan-config.svg)](https://maven-badges.herokuapp.com/maven-central/org.zalando/baigan-config)
[![Build Status](https://img.shields.io/travis/lukasniemeier-zalando/baigan-config/master.svg)](https://travis-ci.org/lukasniemeier-zalando/baigan-config)

Baigan configuration module providing a Spring Boot AutoConfiguration.

## Usage

Using this module, Baigan configuration is active out-of-the-box.
All you need to do is to define your `ConfigurationStore` bean, 
either by programmatically providing a `@Bean` or via the `application.yaml`:

```
baigan:
  store:
    type: namespaced
    stores:
      FeatureAlpha:
        type: local-file
        cache: 2m
        path: file.json
        format: json
      FeatureBeta:
        type: chained
        stores:
          - type: s3-file
            lazy: true
            bucket: my-bucket
            key: my-key
            format: yaml
          - type: etcd-file
            cache: PT15M
            uri: http://localhost/v2/keys/file
            format: yaml
      FeatureGamma:
        type: etcd
        lazy: true
        cache: PT2M
        base-uri: http://localhost/v2/keys/
        format: yaml
```

## Configuration

You can define your `ConfigurationStore` via the `application.yaml` (see example above).

| type of store | key      | type     | default | description                                                |
|---------------|----------|----------|---------|------------------------------------------------------------|
| *any*         | lazy     | boolean  | false   | Indicates whether this store should be initialized lazily. |
| chained       | stores   | List     |         | List of stores to be chained.                              |
| namespaced    | stores   | Map      |         | Map of namespace to store.                                 |
| local-file    | cache    | Duration | 2 min   | Duration the configuration file is cached.                 |
| local-file    | format   | Format   | JSON    | Format of the configuration file (JSON or YAML).           |
| local-file    | path     | String   |         | Path to the configuration file.                            |
| s3-file       | cache    | Duration | 2 min   | Duration the configuration file is cached.                 |
| s3-file       | bucket   | String   |         | S3 bucket name.                                            |
| s3-file       | key      | String   |         | S3 Object key.                                             |
| s3-file       | format   | Format   | JSON    | Format of the configuration file (JSON or YAML).           |
| etcd-file     | cache    | Duration | 2 min   | Duration the configuration file.                           |
| etcd-file     | uri      | String   |         | URI of the etcd key storing the configuration file.        |
| etcd-file     | format   | Format   | JSON    | Format of the configuration file (JSON or YAML).           |
| etcd          | cache    | Duration | 2 min   | Duration individual configurations are cached.             |
| etcd          | base-uri | String   |         | Base URI of the etcd cluster.                              |
| etcd          | format   | Format   | JSON    | Format of the configurations.                              |

## Dependencies

* Spring Boot 2.1+
