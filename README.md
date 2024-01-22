# Baigan configuration

![Build Status](https://github.com/zalando-stups/baigan-config/workflows/build/badge.svg)
[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/baigan-config.svg)](https://maven-badges.herokuapp.com/maven-central/org.zalando/baigan-config)

Baigan configuration is an easy-to-use configuration framework for [Spring](https://spring.io/) based applications. 

Please refer to the [wiki](https://github.com/zalando-stups/baigan-config/wiki) to know more about usage, information, HOWTO, etc.

## 0.18.0 + 0.19.0 + 0.19.1 releases
With certain JDK/JRE versions used, annotated configuration interfaces were not registered as beans. Be aware, that this issue does not occur when application code is being executed by a test runner or alike, only in production setups. Therefore, we recommend using a higher version to avoid this.

## License

Copyright 2016 Zalando SE

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
