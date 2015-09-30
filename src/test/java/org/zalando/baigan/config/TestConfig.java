/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zalando.baigan.config;

import org.springframework.context.annotation.Import;
import org.zalando.baigan.annotation.BaiganConfig;
import org.zalando.baigan.proxy.ConfigurationBeanDefinitionRegistrar;

@BaiganConfig
@Import(ConfigurationBeanDefinitionRegistrar.class)
public interface TestConfig {
    public Boolean enableXyzFeature();
}
