package org.zalando.baigan.proxy.handler;

import com.google.common.base.Supplier;
import com.google.common.primitives.Primitives;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.zalando.baigan.context.ContextProviderRetriever;
import org.zalando.baigan.model.Configuration;
import org.zalando.baigan.provider.ContextProvider;
import org.zalando.baigan.proxy.ProxyUtils;
import org.zalando.baigan.service.ConditionsProcessor;
import org.zalando.baigan.service.ConfigurationRepository;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Suppliers.memoize;
import static org.zalando.baigan.proxy.ProxyUtils.createKey;

/**
 * This class provides a concrete implementation for the Method invocation
 * handler.
 *
 * @author mchand
 */
@Service
public class ContextAwareConfigurationMethodInvocationHandler
        extends ConfigurationMethodInvocationHandler implements BeanFactoryAware {

    private final Logger LOG = LoggerFactory
            .getLogger(ConfigurationMethodInvocationHandler.class);

    private Supplier<ConfigurationRepository> configurationRepository;

    private Supplier<ConditionsProcessor> conditionsProcessor;

    private Supplier<ContextProviderRetriever> contextProviderRetriever;

    /**
     * We have to defer dependency injection and bean resolution as this bean is required by the
     * {@link org.zalando.baigan.proxy.ConfigurationServiceBeanFactory}, which is loaded very
     * early by {@link org.springframework.beans.factory.config.BeanPostProcessor}s.
     */
    @Override
    public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
        this.configurationRepository = memoize(() -> beanFactory.getBean(ConfigurationRepository.class));
        this.conditionsProcessor = memoize(() -> beanFactory.getBean(ConditionsProcessor.class));
        this.contextProviderRetriever = memoize(() -> beanFactory.getBean(ContextProviderRetriever.class));
    }

    @Override
    protected Object handleInvocation(Object proxy, Method method,
                                      Object[] args) throws Throwable {
        final String key = createKey(getClass(proxy), method);
        final Object result = getConfig(key);
        if (result == null) {
            LOG.warn("Configuration not found for key: {}", key);
            return null;
        }

        final Class<?> declaredReturnType = method.getReturnType();

        try {

            Constructor<?> constructor;
            if (declaredReturnType.isInstance(result)) {
                return result;
            } else if (declaredReturnType.isPrimitive()) {
                final Class<?> resultClass = result.getClass();
                constructor = Primitives.wrap(declaredReturnType)
                        .getDeclaredConstructor(resultClass);
            } else if (declaredReturnType.isEnum()) {
                for (Object t : Arrays
                        .asList(declaredReturnType.getEnumConstants())) {
                    if (result.toString().equalsIgnoreCase(t.toString())) {
                        return t;
                    }
                }
                LOG.warn("Unable to map [{}] to enum type [{}].", result, declaredReturnType.getName());
                return null;
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

    private Class<?> getClass(final Object proxy) {
        final Class<?>[] interfaces = proxy.getClass().getInterfaces();
        checkState(interfaces.length == 1, "Expected exactly one interface on proxy object.");
        return interfaces[0];
    }

    private Object getConfig(final String key) {

        final Optional<Configuration> optional = configurationRepository.get().get(key);
        if (!optional.isPresent()) {
            return null;
        }

        final Map<String, String> context = new HashMap<>();

        final ContextProviderRetriever contextProviderRetriever = this.contextProviderRetriever.get();
        for (final String param : contextProviderRetriever.getContextParameterKeys()) {
            Collection<ContextProvider> providers = contextProviderRetriever.getProvidersFor(param);
            if (CollectionUtils.isEmpty(providers)) {
                continue;
            }
            final ContextProvider provider = providers.iterator().next();
            context.put(param, provider.getContextParam(param));
        }

        return conditionsProcessor.get().process(optional.get(), context);

    }
}
