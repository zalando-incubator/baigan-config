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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.zalando.baigan.annotation.BaiganConfig;
import org.zalando.baigan.proxy.handler.ContextAwareMethodInvocationHandler;
import org.zalando.baigan.service.ConfigService;

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

    private ConfigService configService;

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

        final ContextAwareMethodInvocationHandler invocationHandler = applicationContext
                .getBean(ContextAwareMethodInvocationHandler.class);
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

    public void setConfigService(final ConfigService configService) {
        this.configService = configService;
    }

    public ConfigService getService() {
        return this.configService;
    }

}
