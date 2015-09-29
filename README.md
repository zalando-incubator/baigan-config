# Baigan configuration

Baigan configuration is a easy to use configuration framework.
Baigan out-of the box uses [etcd](https://github.com/coreos/etcd) to provide distributed, highly available and reliable configuration cluster.
What makes Baigan a rockstar configuration framework ?

* *Simple*: Baigan configuration is as simple as annotating a Java class.
* *Extensible*: Extend configurations, create rules, define types that suits you.
* *Distributed*: Baigan uses etcd cluster to persis the configurations, which means that in an auto-scalable cluster setting you dont have to worry about updating configuration on each of your server isntance anymore.
 
## Prerequisites
- Java 1.8
- [etcd](https://github.com/coreos/etcd)


## Getting started

### To build project execute on root folder:

    mvn clean install -Pintegration-test
    
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


## Examples


### Equals 
This is a sample configuration

```json
{
  "alias": "express.feature.toggle",
  "description": "Feature toggle",
  "defaultValue": false,
  "conditions": [
    {
      "value": true,
      "conditionType": {
        "onValue": "3",
        "type": "Equals"
      },
      "paramName": "appdomain"
    }
  ] 
}
```

## License

Copyright 2015 Zalando SE

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
