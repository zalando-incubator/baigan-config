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

package org.zalando.baigan.context;

import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.baigan.model.Condition;
import org.zalando.baigan.service.ConditionsProcessor;
import org.zalando.baigan.service.ConfigurationRespository;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

/**
 * @author mchand
 */
@Configuration
class SpringTestContext {

    @Bean
    public ConditionsProcessor ConditionsProcessor() {
        return new ConditionsProcessor();
    }

    @Bean
    public ConfigurationRespository configurationRespository() {
        return new ConfigurationRespository() {
            public final static String KEY = "test.config.enable.xyz.feature";

            public void put(String key, String value) {
            }

            public Optional<org.zalando.baigan.model.Configuration<?>> getConfig(
                    String key) {
                if (KEY.equalsIgnoreCase(key)) {
                    return Optional.of(mockConfiguration(key));
                } else {
                    return Optional.absent();
                }

            }

            private org.zalando.baigan.model.Configuration<Boolean> mockConfiguration(
                    final String key) {

                final Set<Condition<Boolean>> conditions = ImmutableSet.of();
                final org.zalando.baigan.model.Configuration<Boolean> configuration = new org.zalando.baigan.model.Configuration<Boolean>(
                        key, "This is a test configuration object.", conditions,
                        Boolean.FALSE);

                return configuration;
            }
        };
    }
}
