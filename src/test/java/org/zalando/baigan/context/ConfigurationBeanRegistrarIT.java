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

import static org.junit.Assert.assertThat;

import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.baigan.annotation.ConfigurationServiceScan;
import org.zalando.baigan.config.TestConfig;
import org.zalando.baigan.context.ConfigurationContextProviderBeanPostprocessor;
import org.zalando.baigan.context.ConfigurationContextProviderRegistryImpl;
import org.zalando.baigan.model.Condition;
import org.zalando.baigan.service.ConditionsProcessor;
import org.zalando.baigan.service.ConfigService;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

/**
 *
 * @author mchand
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        ConfigurationContextProviderRegistryImpl.class,
        ConfigurationContextProviderBeanPostprocessor.class,
        ConfigurationBeanRegistrarIT.class })
@ConfigurationServiceScan({ "org.zalando.baigan.config" })
@ComponentScan(basePackageClasses = {
        org.zalando.baigan.proxy.handler.ConfigurationMethodInvocationHandler.class,
        TestBeans.class })
public class ConfigurationBeanRegistrarIT {

    @Autowired
    private TestConfig config;

    @Test
    public void testConfiguration() {
        assertThat(config.enableXyzFeature(), Matchers.equalTo("false"));
    }

}

@Configuration
class TestBeans {

    @Bean
    public ConditionsProcessor ConditionsProcessor() {
        return new ConditionsProcessor();
    }

    @Bean
    public ConfigService configService() {
        return new ConfigService() {
            public final static String KEY = "test.config.enable.xyz.feature";

            @Override
            public void put(String key, String value) {
            }

            @Override
            public Optional<org.zalando.baigan.model.Configuration> getConfig(
                    String key) {
                if (KEY.equalsIgnoreCase(key)) {
                    return Optional.of(mockConfiguration(key));
                } else {
                    return Optional.absent();
                }

            }

            private org.zalando.baigan.model.Configuration<Boolean> mockConfiguration(
                    final String key) {
                // TODO: Define conditions/use context params here
                // final Set<Condition<Boolean>> conditions = ImmutableSet
                // .of(new Condition<Boolean>("appdomainid",
                // new Equals("1"), Boolean.TRUE));

                final Set<Condition<Boolean>> conditions = ImmutableSet.of();
                final org.zalando.baigan.model.Configuration<Boolean> configuration = new org.zalando.baigan.model.Configuration<Boolean>(
                        key, "This is a test configuration object.", conditions,
                        Boolean.FALSE);

                return configuration;
            }
        };
    }

}
