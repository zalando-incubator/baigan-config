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

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.zalando.baigan.annotation.BaiganConfig;
import org.zalando.baigan.annotation.ConfigurationServiceScan;
import org.zalando.baigan.proxy.ConfigurationBeanDefinitionRegistrar;

import com.google.common.collect.ImmutableMap;

/**
 *
 * @author mchand
 *
 */
@RunWith(JUnit4.class)
public class ConfigurationBeanDefinitionRegistrarTest {

    @Test
    public void testRegistration() {

        final ConfigurationBeanDefinitionRegistrar registrar = new ConfigurationBeanDefinitionRegistrar();

        final AnnotationMetadata metaData = Mockito
                .mock(AnnotationMetadata.class);
        Mockito.when(metaData.getAnnotationAttributes(
                ConfigurationServiceScan.class.getName()))
                .thenReturn(ImmutableMap.of("value",
                        new String[] { "org.zalando.baigan.context" },
                        "basePackages", new String[] {}));

        final BeanDefinitionRegistry registry = Mockito
                .mock(BeanDefinitionRegistry.class);

        final ArgumentCaptor<AbstractBeanDefinition> beanDefinition = ArgumentCaptor
                .forClass(AbstractBeanDefinition.class);
        final ArgumentCaptor<String> beanName = ArgumentCaptor
                .forClass(String.class);
        registrar.registerBeanDefinitions(metaData, registry);

        Mockito.verify(registry).registerBeanDefinition(beanName.capture(),
                beanDefinition.capture());

        assertThat(beanName.getValue(), Matchers.equalTo(
                "org.zalando.baigan.context.SuperSonicBaiganProxyConfigurationFactoryBean"));

        assertThat(beanDefinition.getValue(),
                Matchers.instanceOf(GenericBeanDefinition.class));

    }

}

@BaiganConfig
interface SuperSonic {
    String speed();
}
