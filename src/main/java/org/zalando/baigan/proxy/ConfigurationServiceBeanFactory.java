package org.zalando.baigan.proxy;

import com.google.common.reflect.Reflection;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.zalando.baigan.annotation.BaiganConfig;
import org.zalando.baigan.proxy.handler.ConfigurationMethodInvocationHandler;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Factory class that creates the proxy implementations for the interfaces
 * marked with {@link BaiganConfig}.
 *
 * @author mchand
 */
public class ConfigurationServiceBeanFactory extends AbstractFactoryBean<Object> {

    private Class<?> candidateInterface;

    public void setCandidateInterface(final Class<?> candidateInterface) {
        this.candidateInterface = candidateInterface;
    }

    protected Object createInstance() {

        final BaiganConfig beanConfig = candidateInterface.getAnnotation(BaiganConfig.class);
        checkNotNull(beanConfig,
                "This BeanFactory could only create Beans for classes annotated with "
                        + BaiganConfig.class.getName());

        final ConfigurationMethodInvocationHandler handler = getBeanFactory().getBean(ConfigurationMethodInvocationHandler.class);
        return Reflection.newProxy(candidateInterface, handler);
    }

    @Override
    public Class<?> getObjectType() {
        return this.candidateInterface;
    }

}
