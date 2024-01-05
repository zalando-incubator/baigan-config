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
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.AnnotationMetadata;
import org.zalando.baigan.annotation.ConfigurationServiceScan;
import org.zalando.baigan.context.packagec.Marker;
import org.zalando.baigan.proxy.BaiganConfigClasses;
import org.zalando.baigan.proxy.ConfigurationBeanDefinitionRegistrar;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author mchand
 */
public class ConfigurationBeanDefinitionRegistrarTest {

    private final AnnotationMetadata metaData = mock(AnnotationMetadata.class);
    private final BeanDefinitionRegistry registry = mock(BeanDefinitionRegistry.class);
    private final ConfigurationBeanDefinitionRegistrar registrar = new ConfigurationBeanDefinitionRegistrar();

    @Test
    public void testRegistrationOfBeans() {
        when(metaData.getAnnotationAttributes(ConfigurationServiceScan.class.getName())).thenReturn(
            ImmutableMap.of(
                "value", new String[]{"org.zalando.baigan.context.packagea"},
                "basePackages", new String[]{"org.zalando.baigan.context.packageb"},
                "basePackageClasses", new Class[]{Marker.class}
            )
        );

        final ArgumentCaptor<String> beanName = ArgumentCaptor.forClass(String.class);
        registrar.registerBeanDefinitions(metaData, registry);

        verify(registry, times(4)).registerBeanDefinition(beanName.capture(), any());

        assertThat(
            beanName.getAllValues(),
            containsInAnyOrder(
                "org.zalando.baigan.context.packagea.SuperSonicBaiganProxyConfigurationFactoryBean",
                "org.zalando.baigan.context.packageb.UltraVioletBaiganProxyConfigurationFactoryBean",
                "org.zalando.baigan.context.packagec.LiquidCrystalBaiganProxyConfigurationFactoryBean",
                "baiganConfigClasses"
            )
        );
    }

    @Test
    public void testRegistrationOfBaiganConfigClassesTypes() {
        when(metaData.getAnnotationAttributes(ConfigurationServiceScan.class.getName())).thenReturn(
            ImmutableMap.of(
                "value", new String[]{"org.zalando.baigan.context.packagea"},
                "basePackages", new String[]{"org.zalando.baigan.context.packageb"},
                "basePackageClasses", new Class[]{Marker.class}
            )
        );

        final ArgumentCaptor<AbstractBeanDefinition> beanDefinition = ArgumentCaptor.forClass(
            AbstractBeanDefinition.class
        );
        registrar.registerBeanDefinitions(metaData, registry);

        verify(registry, atLeastOnce()).registerBeanDefinition(anyString(), beanDefinition.capture());

        List<AbstractBeanDefinition> definitions = beanDefinition.getAllValues();
        AbstractBeanDefinition classesDefinition = definitions.stream()
            .filter(def -> def.getBeanClass().equals(BaiganConfigClasses.class))
            .findFirst().orElseThrow(AssertionError::new);
        assertThat(
            classesDefinition.getPropertyValues().get("configTypesByKey"),
            equalTo(
                Map.of(
                    "super.sonic.speed", String.class,
                    "ultra.violet.wavelength", Integer.class,
                    "liquid.crystal.mass", Float.class
                )
            )
        );
    }


    @Test
    public void whenNothingAnnotatedWithConfigurationServiceScan_shouldThrowException() {
        when(metaData.getAnnotationAttributes(ConfigurationServiceScan.class.getName())).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> registrar.registerBeanDefinitions(metaData, registry));

        when(metaData.getAnnotationAttributes(ConfigurationServiceScan.class.getName())).thenReturn(ImmutableMap.of());
        assertThrows(IllegalArgumentException.class, () -> registrar.registerBeanDefinitions(metaData, registry));
    }

    @Test
    public void whenConfigurationContainsPrimitiveTypes_shouldThrowException() {
        when(metaData.getAnnotationAttributes(ConfigurationServiceScan.class.getName())).thenReturn(
                ImmutableMap.of(
                        "value", new String[]{"org.zalando.baigan.context.packaged"},
                        "basePackages", new String[]{},
                        "basePackageClasses", new Class[]{}
                )
        );

        assertThrows(IllegalArgumentException.class, () -> registrar.registerBeanDefinitions(metaData, registry));
    }
}
