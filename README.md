# Baigan configuration

Baigan configuration is a easy to use configuration framework.
Baigan out-of the box uses [etcd](https://github.com/coreos/etcd) to provide distributed, highly available and reliable configuration cluster.
What makes Baigan a rockstar configuration framework ?

* *Simple*: Baigan configuration is as simple as annotating a Java class.
* *Extensible*: Extend configurations, create rules, define types that suits you.
* *Distributed*: Baigan uses etcd cluster to persis the configurations, which means that in an auto-scalable cluster setting you dont have to worry about updating configuration on each of your server isntance anymore.
 
## Prerequisites
- Java 1.8
- [etcd](https://github.com/coreos/etcd) started and running on default port.


## Getting started

### To build project execute on root folder:

```bash
    mvn clean install -Pintegration-test
```
### Integrating Baigan config
Baigan config is a spring project. The larger part of integration involves configuring beans to facilitate the spring beans.



#### 1. Prepare etcd

##### a. Define $ETCD_URL environment variable with the url of your ETCD cluster.
```bash
 #example
 export ETCD_URL=http://127.0.0.1:2379
````
  

##### b. Create a configuration
To create a key in etcd, you can either use the etcd client etcdctl or the good old curl in the following way.
This sample json defines a configuration for key _express.feature.enabled_ that the value is _true_ if the _country_code_ is 3, with the default value being _false_.

Save this content in a file named _express-feature.json_

```json
value= '{
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
}'
```

Put the new etcd key with _curl_ cli

```bash
curl -v -XPUT http://127.0.0.1:2379/v2/keys/express.feature.enabled -d @express-feature.json
```


#### 2. Configuring components and Configuration interface scanning.

```Java

import org.zalando.baigan.BaiganSpringContext;

@ComponentScan(basePackageClasses = {
                BaiganSpringContext.class })
@ConfigurationServiceScan(basePackages = {
        "com.foo.configurations" })
public class Application {
}
```

The _BaiganSpringContext_ class includes the Baigan-Config beans required to be loaded into the spring application context.
And the _@ConfigurationServiceScan_ annotation hints the Baigan registrar to look into the packages where the _@BaiganConfig_ annotated interfaces could be found.

 
#### 3. Annotate your configuration interfaces with _@BaiganConfig_

```Java
	@BaiganConfig
	public interface ExpressFeature {
	
	    public Boolean enabled();
	    
	    public String serviceUrl();

	}
```

The above example code enables the application to inject _ExpressFeature_ spring bean into any Spring bean and do the following:

```Java
	@Component
	public class ExpressServiceImpl implements ExpressService{
 
		@Inject
		private ExpressFeature expressFeature;
	
		@Override
		public void sendShipment(final Shipment shipment){
			if (expressFeature.enabled()){
				final String serviceUrl = expressFeature.serviceUrl();
			}
		}
	}
```
    
## Configuration schema
Configurations are stored in its simplest form as key values.
A configuration is a pair of a dot(.) separated key and a value object in JSON format.

A configuration object has the following JSON Schema:

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
                        "type": "object",
                    }
                }
            }
        }
    },
    "required": ["defaultValue"]
}
```


## License

Copyright 2015 Zalando SE

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
