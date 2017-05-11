package org.zalando.baigan.proxy;

import com.google.common.base.Preconditions;
import com.google.common.reflect.Reflection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.zalando.baigan.annotation.BaiganConfig;
import org.zalando.baigan.proxy.handler.ConfigurationMethodInvocationHandler;

/**
 * Factory class that creates the proxy implementations for the interfaces
 * marked with {@link BaiganConfig}.
 *
 * @author mchand
 *
 */
public class ConfigurationServiceBeanFactory extends AbstractFactoryBean<Object> {

    private Class<?> candidateInterface;

    private final ConfigurationMethodInvocationHandler methodInvocationHandler;

    @Autowired
    public ConfigurationServiceBeanFactory(final ConfigurationMethodInvocationHandler methodInvocationHandler) {
        this.methodInvocationHandler = methodInvocationHandler;
    }

    public void setCandidateInterface(final Class<?> candidateInterface) {
        this.candidateInterface = candidateInterface;
    }

    protected Object createInstance() {

        final BaiganConfig beanConfig = candidateInterface
                .getAnnotation(BaiganConfig.class);
        Preconditions.checkNotNull(beanConfig,
                "This BeanFactory could only create Beans for classes annotated with "
                        + BaiganConfig.class.getName());

        return Reflection.newProxy(candidateInterface, methodInvocationHandler);
    }

    @Override
    public Class<?> getObjectType() {
        return this.candidateInterface;
    }

}
