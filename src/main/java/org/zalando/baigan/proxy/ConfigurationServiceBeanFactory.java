package org.zalando.baigan.proxy;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.zalando.baigan.annotation.BaiganConfig;
import org.zalando.baigan.proxy.handler.ConfigurationMethodInvocationHandler;
import org.zalando.baigan.service.ConfigurationRepository;

import com.google.common.base.Preconditions;
import com.google.common.reflect.Reflection;

/**
 * Factory class that creates the proxy implementations for the interfaces
 * marked with {@link BaiganConfig}.
 *
 * @author mchand
 *
 */
public class ConfigurationServiceBeanFactory extends AbstractFactoryBean<Object>
        implements ApplicationContextAware {

    private Class<?> candidateInterface;

    private ConfigurationRepository configurationRepository;

    private ApplicationContext applicationContext;

    public void setCandidateInterface(final Class<?> candidateInterface) {
        this.candidateInterface = candidateInterface;
    }

    /**
     * Returns a proxy that implements the given interface.
     *
     */

    protected Object createInstance() {

        final BaiganConfig beanConfig = candidateInterface
                .getAnnotation(BaiganConfig.class);
        Preconditions.checkNotNull(beanConfig,
                "This BeanFactory could only create Beans for classes annotated with "
                        + BaiganConfig.class.getName());

        final ConfigurationMethodInvocationHandler invocationHandler = applicationContext
                .getBean(ConfigurationMethodInvocationHandler.class);
        return Reflection.newProxy(candidateInterface, invocationHandler);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;

    }

    @Override
    public Class<?> getObjectType() {
        return this.candidateInterface;
    }

    public void setConfigurationRepository(final ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    public ConfigurationRepository getConfigurationRespository() {
        return this.configurationRepository;
    }

}
