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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.zalando.baigan.provider.ContextProvider;

/**
 * This class implements the {@link BeanPostProcessor} interface to register all
 * the {@link ContextProvider}s in the default {@link ContextProviderRegistry}
 *
 * @author mchand
 * @since 1.0
 */
@Component
public class ConfigurationContextProviderBeanPostprocessor
        implements BeanPostProcessor {

    private final ObjectFactory<ContextProviderRegistry> registry;

    @Autowired
    public ConfigurationContextProviderBeanPostprocessor(final ObjectFactory<ContextProviderRegistry> registry) {
        this.registry = registry;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean,
            final String beanName) throws BeansException {
        if (ContextProvider.class.isInstance(bean)) {
            final ContextProvider contextProvider = (ContextProvider) bean;
            registry.getObject().register(contextProvider);
        }
        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean,
            final String beanName) throws BeansException {
        return bean;
    }
}
