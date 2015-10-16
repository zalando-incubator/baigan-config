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

package org.zalando.baigan.context;

import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.zalando.baigan.proxy.handler.ContextAwareConfigurationMethodInvocationHandler;
import org.zalando.baigan.service.ConditionsProcessor;
import org.zalando.baigan.service.ConfigurationRespository;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

/**
 *
 * @author mchand
 *
 */
@RunWith(JUnit4.class)
public class MethodInvocationHandlerTest {

    @Test
    public void testEnumValue() throws Throwable {

        final ConfigurationRespository repo = Mockito
                .mock(ConfigurationRespository.class);
        final org.zalando.baigan.model.Configuration<String> configuration = new org.zalando.baigan.model.Configuration<String>(
                "express.state.default", "This is a test configuration object.",
                ImmutableSet.of(), "SHIPPING");
        Mockito.when(repo.getConfig(org.mockito.Matchers.anyString()))
                .thenReturn(Optional.of(configuration));

        final ContextProviderRetriever retriever = Mockito.mock(
                ContextProviderRetriever.class,
                org.mockito.Answers.RETURNS_SMART_NULLS.toString());

        ContextAwareConfigurationMethodInvocationHandler handler = new ContextAwareConfigurationMethodInvocationHandler(
                repo, new ConditionsProcessor(), retriever);

        Method method = Express.class.getMethod("stateDefault");
        Object object = handler.invoke("", method, new String[] {});
        assertThat(object, Matchers.equalTo(State.SHIPPING));
    }

    @Test
    public void testPrimitiveType() throws Throwable {

        final ConfigurationRespository repo = Mockito
                .mock(ConfigurationRespository.class);
        final org.zalando.baigan.model.Configuration<String> configuration = new org.zalando.baigan.model.Configuration<String>(
                "express.max.delivery.days",
                "This is a test configuration object.", ImmutableSet.of(), "3");
        Mockito.when(repo.getConfig(org.mockito.Matchers.anyString()))
                .thenReturn(Optional.of(configuration));

        final ContextProviderRetriever retriever = Mockito.mock(
                ContextProviderRetriever.class,
                org.mockito.Answers.RETURNS_SMART_NULLS.toString());

        ContextAwareConfigurationMethodInvocationHandler handler = new ContextAwareConfigurationMethodInvocationHandler(
                repo, new ConditionsProcessor(), retriever);

        Method method = Express.class.getMethod("maxDeliveryDays");
        Object object = handler.invoke("", method, new String[] {});
        assertThat(object, Matchers.equalTo(3));
    }

}

interface Express {
    State stateDefault();

    int maxDeliveryDays();
}

enum State {
    SHIPPING, SHIPPED, DELIVERED
}