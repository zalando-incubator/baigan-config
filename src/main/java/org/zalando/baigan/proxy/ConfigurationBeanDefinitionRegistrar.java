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

package org.zalando.baigan.proxy;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;
import org.zalando.baigan.annotation.BaiganConfig;
import org.zalando.baigan.annotation.ConfigurationServiceScan;

import com.google.common.collect.Lists;

/**
 * ImportBeanDefinitionRegistrar implementation that finds the
 * {@link ConfigurationServiceScan} annotations, delegates the scanning of
 * packages and proxy bean creation further down to the corresponding
 * implementations.
 *
 * @see ConfigurationServiceBeanFactory
 *
 * @author mchand
 *
 */
public class ConfigurationBeanDefinitionRegistrar
        implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(
            AnnotationMetadata importingClassMetadata,
            BeanDefinitionRegistry registry) {
        final AnnotationAttributes annotationAttributes = AnnotationAttributes
                .fromMap(importingClassMetadata.getAnnotationAttributes(
                        ConfigurationServiceScan.class.getName()));

        if (annotationAttributes == null || annotationAttributes.isEmpty()) {
            throw new IllegalArgumentException(
                    "ConfigurationServiceScan requires at least 1 scan package.");
        }

        final List<String> basePackages = Lists.newArrayList();
        basePackages.addAll(
                Arrays.asList(annotationAttributes.getStringArray("value")));
        basePackages.addAll(Arrays
                .asList(annotationAttributes.getStringArray("basePackages")));

        final Set<String> saneSet = basePackages.stream()
                .filter(str -> !StringUtils.isEmpty(str))
                .collect(Collectors.toSet());

        createAndRegisterBeanDefinitions(saneSet, registry);

    }

    private void createAndRegisterBeanDefinitions(final Set<String> packages,
            final BeanDefinitionRegistry registry) {
        for (final String singlePackage : packages) {

            final Set<Class<?>> configServiceInterfaces = new Reflections(
                    singlePackage).getTypesAnnotatedWith(BaiganConfig.class);

            for (final Class<?> interfaceToImplement : configServiceInterfaces) {
                final BeanDefinitionBuilder builder = BeanDefinitionBuilder
                        .genericBeanDefinition(
                                ConfigurationServiceBeanFactory.class);
                builder.addPropertyValue("candidateInterface",
                        interfaceToImplement);

                final String factoryBeanName = interfaceToImplement.getName()
                        + "BaiganProxyConfigurationFactoryBean";
                registry.registerBeanDefinition(factoryBeanName,
                        builder.getBeanDefinition());
            }

        }
    }

}
