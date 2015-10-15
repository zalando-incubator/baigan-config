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

package org.zalando.baigan.proxy.handler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.zalando.baigan.context.ContextProviderRetriever;
import org.zalando.baigan.model.Configuration;
import org.zalando.baigan.provider.ContextProvider;
import org.zalando.baigan.proxy.ProxyUtils;
import org.zalando.baigan.service.ConditionsProcessor;
import org.zalando.baigan.service.ConfigurationRespository;

import com.google.common.base.Optional;
import com.google.common.primitives.Primitives;

/**
 * This class provides a concrete implementation for the Method invocation
 * handler.
 *
 * @author mchand
 *
 */
@Service
public class ContextAwareConfigurationMethodInvocationHandler
        extends ConfigurationMethodInvocationHandler {

    private final Logger LOG = LoggerFactory
            .getLogger(ConfigurationMethodInvocationHandler.class);

    @Autowired
    private ConfigurationRespository configurationRepository;

    @Autowired
    private ConditionsProcessor conditionsProcessor;

    @Autowired
    private ContextProviderRetriever contextProviderRetriever;

    @Override
    protected Object handleInvocation(Object proxy, Method method,
            Object[] args) throws Throwable {
        final String methodName = method.getName();

        final String nameSpace = method.getDeclaringClass().getSimpleName();

        final String key = ProxyUtils.dottify(nameSpace) + "."
                + ProxyUtils.dottify(methodName);
        final Object result = getConfig(key);
        if (result == null) {
            LOG.warn("Configuration not found for key: {}", key);
            return null;
        }

        final Class<?> declaredReturnType = method.getReturnType();

        try {

            Constructor<?> constructor = null;
            if (declaredReturnType.isInstance(result)) {
                return result;
            } else if (declaredReturnType.isPrimitive()) {
                final Class<?> resultClass = result.getClass();
                constructor = Primitives.wrap(declaredReturnType)
                        .getDeclaredConstructor(resultClass);
            } else {
                constructor = declaredReturnType
                        .getDeclaredConstructor(result.getClass());
            }
            return constructor.newInstance(result);
        } catch (Exception exception) {
            LOG.warn(
                    "Wrong or Incompatible configuration. Cannot find a constructor to create object of type "
                            + declaredReturnType
                            + " for value of the configuration key " + key,
                    exception);
        }
        return null;
    }

    private Object getConfig(final String key) {

        Optional<Configuration<?>> optional = configurationRepository
                .getConfig(key);
        if (!optional.isPresent()) {
            return null;
        }

        final Map<String, String> context = new HashMap<String, String>();

        for (final String param : contextProviderRetriever
                .getContextParameterKeys()) {
            Collection<ContextProvider> providers = contextProviderRetriever
                    .getProvidersFor(param);
            if (CollectionUtils.isEmpty(providers)) {
                continue;
            }
            final ContextProvider provider = providers.iterator().next();
            context.put(param, provider.getContextParam(param));
        }

        final Configuration<?> configuration = optional.get();
        final Object result = conditionsProcessor.process(configuration,
                context);
        return result;

    }
}
