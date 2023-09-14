/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zalando.baigan.context;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.zalando.baigan.annotation.BaiganConfig;
import org.zalando.baigan.annotation.ConfigurationServiceScan;
import org.zalando.baigan.proxy.ConfigurationBeanDefinitionRegistrar;

import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author mchand
 */
public class ConfigurationBeanDefinitionRegistrarTest {

    @Test
    public void testRegistration() {

        final ConfigurationBeanDefinitionRegistrar registrar = new ConfigurationBeanDefinitionRegistrar();

        final AnnotationMetadata metaData = Mockito
                .mock(AnnotationMetadata.class);
        Mockito.when(metaData.getAnnotationAttributes(
                        ConfigurationServiceScan.class.getName()))
                .thenReturn(ImmutableMap.of("value",
                        new String[]{"org.zalando.baigan.context"},
                        "basePackages", new String[]{}));

        final BeanDefinitionRegistry registry = Mockito
                .mock(BeanDefinitionRegistry.class);

        final ArgumentCaptor<AbstractBeanDefinition> beanDefinition = ArgumentCaptor
                .forClass(AbstractBeanDefinition.class);
        final ArgumentCaptor<String> beanName = ArgumentCaptor
                .forClass(String.class);
        registrar.registerBeanDefinitions(metaData, registry);

        verify(registry, times(2)).registerBeanDefinition(beanName.capture(),
                beanDefinition.capture());

        assertThat(beanName.getAllValues(), contains(
                "org.zalando.baigan.context.SuperSonicBaiganProxyConfigurationFactoryBean",
                "baiganConfigClasses")
        );

        assertThat(beanDefinition.getAllValues().get(0), instanceOf(GenericBeanDefinition.class));
        assertThat(beanDefinition.getAllValues().get(1).getPropertyValues().get("configTypesByKey"),
                equalTo(Map.of("super.sonic.speed", String.class)));

    }

}

@BaiganConfig
interface SuperSonic {
    String speed();
}
