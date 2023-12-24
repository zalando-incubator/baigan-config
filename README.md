# Baigan configuration

![Build Status](https://github.com/zalando-stups/baigan-config/workflows/build/badge.svg)
[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/baigan-config.svg)](https://maven-badges.herokuapp.com/maven-central/org.zalando/baigan-config)

Baigan configuration is an easy to use configuration framework for [Spring](https://spring.io/) based applications.

What makes Baigan a rockstar configuration framework ?

* *Simple*: Using Baigan configurations is as simple as annotating a Java interface.
* *Extensible*: Extend configurations, create rules, define types that suit you.
* *Flexible*: Baigan is a client library that can read configurations from multiple repositories:
	* Filesystem
	* AWS S3
	* Etcd

## Prerequisites
- Java 11
- Spring framework
- AWS SDK

## Getting started

### To build the project run:

```bash
    mvn clean install -Pintegration-test
```

### Integrating Baigan config
Baigan config is a spring project. The larger part of integration involves configuring beans to facilitate the spring beans.

#### Configuring components and Configuration interface scanning.

```Java

import org.zalando.baigan.BaiganSpringContext;

@ComponentScan(basePackageClasses = {BaiganSpringContext.class })
@ConfigurationServiceScan(basePackages = {"com.foo.configurations" })
public class Application {
}
```

The _BaiganSpringContext_ class includes the Baigan-Config beans required to be loaded into the spring application context.
And the _@ConfigurationServiceScan_ annotation hints the Baigan registrar to look into the packages where the _@BaiganConfig_ annotated interfaces could be found.

#### Annotate your configuration interfaces with _@BaiganConfig_

```Java
	@BaiganConfig
	public interface ExpressFeature {

	    Boolean enabled();

	    String serviceUrl();

        SomeStructuredConfigClass complexConfiguration();
        
        List<String> configList();

		Map<UUID, List<SomeConfigObject>> nestedGenericConfiguration();
	}
```

The individual methods may have arbitrary classes as return types, in particular complex structured types are supported, including Generics.

**Note**: Primitives are not supported as return types as they cannot be null and therefore cannot express a missing configuration value.

> [!CAUTION]
> Primitives are not supported as return types as they cannot be null and therefore cannot express a missing configuration value.
> If you use Baigan with Kotlin, it means you need to use nullable primitive types, e.g. `Int?` instead of `Int`.

The above example code enables the application to inject _ExpressFeature_ spring bean into any other Spring bean:

```Java
	@Component
	public class ExpressServiceImpl implements ExpressService{

		@Inject
		private ExpressFeature expressFeature;

		@Override
		public void sendShipment(final Shipment shipment){
			if (expressFeature.enabled()){
				final String serviceUrl = expressFeature.serviceUrl();
				// Use the configuration
			}
		}
	}
```

#### Provide a configuration repository

Finally, a `ConfigurationRepository` Spring Bean has to be provided that can provide the configuration values.
This is done using the Spring Bean of type `RepositoryFactory`, which allows creating builders for all repository
types. The following example shows how to configure a filesystem based repository.

```Java
	@Configuration
	public class ApplicationConfiguration {
		
    	@Bean
		public ConfigurationRepository configurationRepository(RepositoryFactory factory){
			return factory.fileSystemConfigurationRepository()
						  .fileName("configs.json");
		}
	}
``` 

Check the documentation of the builders for details on how to configure the repositories. In particular, all
repositories can be configured with a Jackson `ObjectMapper` used to deserialize the configuration.

### Creating configurations
Baigan configurations follow a specific schema and can be stored on any of the supported repositories.

#### Configuration schema
Configurations are stored in its simplest form as key values.
A configuration is a pair of a dot(.) separated key and a value objects in JSON format.

A configuration object should conform to the following JSON Schema:

```json
{
    "$schema": "http://json-schema.org/draft-04/schema#",
    "title": "Configuration",
    "description": "A baigan configuration object value.",
    "type": "object",
    "properties": {
        "alias": {
            "description": "The identifier for the configuration, same as its key.",
            "type": "string"
        },
         "description": {
            "description": "Summary of the configuration.",
            "type": "string"
        },
         "defaultValue": {
            "description": "Default configuration if none of the condition is satisfied.",
            "type": {}
        },
         "conditions": {
            "description": "List of conditions to check",
            "type": "array",
            "items": {
            	"type": "object",
            	"properties": {
                    "value": {
                        "description": "Configuration value if this condition evaluates to true.",
                        "type": {}
                    },
            		"conditionType": {
                        "description": "Type of condition to evaluate. This can be custom defined, with custom defined properties.",
                        "type": "object"
                    }
                }
            }
        }
    },
    "required": ["defaultValue"]
}
```

#### Example configurations

This sample JSON defines a configuration for the key `express.feature.enabled` with the value _true_ when the _country_code_ is 3, and a default value of _false_.

```json
{
  "alias": "express.feature.enabled",
  "description": "Feature toggle",
  "defaultValue": false,
  "conditions": [
    {
      "value": true,
      "conditionType": {
        "onValue": "3",
        "type": "Equals"
      },
      "paramName": "country_code"
    }
  ]
}
```

#### Pushing configuration to repositories
This step depends on the chosen repository. 

##### Filesystem
Save a file named express-feature.json with the content above anywhere on the filesystem and bundle it as part of your application. To use it just specify the classpath in the constructor.

##### AWS S3
Save a file named express-feature.json with the content above and upload it to any S3 bucket. To use it just provide the bucket name and the object key.

##### Etcd
To create a key in etcd, you can either use etcdctl or the good old curl in the following way.
Save a file named express-feature.json with the content above and push it to your etcd cluster:

With [etcdctl v2](https://github.com/coreos/etcd/blob/master/etcdctl/READMEv2.md):
```bash
etcdctl set express.feature.enabled < express-feature.json 
```

With curl:
```bash
curl -v -XPUT http://127.0.0.1:2379/v2/keys/express.feature.enabled -d value="$(cat express-feature.json)"
```

## 0.18.0 + 0.19.0 + 0.19.1 releases
With certain JDK/JRE versions used, annotated configuration interfaces were not registered as beans. Be aware, that this issue does not occur when application code is being executed by a test runner or alike, only in production setups. Therefore, we recommend using a higher version to avoid this.

## License

Copyright 2016 Zalando SE

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
