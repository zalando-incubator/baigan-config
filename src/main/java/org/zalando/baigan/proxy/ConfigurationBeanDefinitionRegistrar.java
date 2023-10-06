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

package org.zalando.baigan.proxy;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;
import org.zalando.baigan.annotation.BaiganConfig;
import org.zalando.baigan.annotation.ConfigurationServiceScan;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.zalando.baigan.proxy.ProxyUtils.createKey;

/**
 * ImportBeanDefinitionRegistrar implementation that finds the
 * {@link ConfigurationServiceScan} annotations, delegates the scanning of
 * packages and proxy bean creation further down to the corresponding
 * implementations.
 *
 * @author mchand
 * @see ConfigurationServiceBeanFactory
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

        final ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateInterfaceProvider();
        scanner.addIncludeFilter(new AnnotationTypeFilter(BaiganConfig.class));

        List<Class<?>> baiganConfigClasses = new ArrayList<>();

        for (final String singlePackage : packages) {
            final Set<BeanDefinition> candidates = scanner.findCandidateComponents(singlePackage);
            for (BeanDefinition definition : candidates) {
                if (definition instanceof GenericBeanDefinition) {
                    final GenericBeanDefinition genericDefinition = (GenericBeanDefinition) definition;
                    final Class<?> baiganConfigClass = registerAsBean(registry, genericDefinition);
                    baiganConfigClasses.add(baiganConfigClass);
                } else {
                    throw new IllegalStateException(
                            String.format(
                                    "Unable to read required metadata of configuration candidate [%s]",
                                    definition
                            )
                    );
                }
            }
        }
        Map<String, Type> configTypesByKey = baiganConfigClasses.stream().flatMap(clazz ->
                Arrays.stream(clazz.getMethods()).map(method -> new ConfigType(createKey(clazz, method), method.getGenericReturnType()))
        ).collect(toMap(c -> c.key, c -> c.type));
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(BaiganConfigClasses.class);
        beanDefinition.getPropertyValues().add("configTypesByKey", configTypesByKey);
        registry.registerBeanDefinition("baiganConfigClasses", beanDefinition);
    }

    private Class<?> registerAsBean(final BeanDefinitionRegistry registry, final GenericBeanDefinition genericDefinition) {
        try {
            final Class<?> interfaceToImplement = genericDefinition.resolveBeanClass(
                    registry.getClass().getClassLoader()
            );
            registerAsBean(registry, interfaceToImplement);
            return interfaceToImplement;
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException("Unable to register annotated interface as configuration bean", e);
        }
    }

    private void registerAsBean(final BeanDefinitionRegistry registry, final Class<?> interfaceToImplement) {
        final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(
                ConfigurationServiceBeanFactory.class
        );
        builder.addPropertyValue("candidateInterface", interfaceToImplement);

        final String factoryBeanName = interfaceToImplement.getName() + "BaiganProxyConfigurationFactoryBean";
        registry.registerBeanDefinition(factoryBeanName, builder.getBeanDefinition());
    }

    private static class ClassPathScanningCandidateInterfaceProvider
            extends ClassPathScanningCandidateComponentProvider {

        public ClassPathScanningCandidateInterfaceProvider() {
            super(false);
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            final AnnotationMetadata metadata = beanDefinition.getMetadata();
            return metadata.isIndependent() && metadata.isInterface();
        }
    }

    private static class ConfigType {
        private final String key;
        private final Type type;

        public ConfigType(String key, Type type) {
            this.key = key;
            this.type = type;
        }
    }
}
