package org.zalando.baigan.context;

import com.google.common.collect.ImmutableSet;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.zalando.baigan.model.Configuration;
import org.zalando.baigan.proxy.handler.ContextAwareConfigurationMethodInvocationHandler;
import org.zalando.baigan.service.ConditionsProcessor;
import org.zalando.baigan.service.ConfigurationRepository;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

enum State {
    SHIPPING, SHIPPED, DELIVERED
}

interface Express {
    State stateDefault();

    int maxDeliveryDays();
}

/**
 * @author mchand
 */
@RunWith(JUnit4.class)
public class MethodInvocationHandlerTest {

    @Test
    public void testEnumValue() throws Throwable {

        final ConfigurationRepository repo = mock(ConfigurationRepository.class);
        final Configuration<String> configuration = new Configuration<>(
                "express.state.default", "This is a test configuration object.",
                ImmutableSet.of(), "SHIPPING");
        when(repo.get(anyString())).thenReturn(Optional.of(configuration));

        final ContextProviderRetriever retriever = mock(
                ContextProviderRetriever.class,
                org.mockito.Answers.RETURNS_SMART_NULLS.toString());

        ContextAwareConfigurationMethodInvocationHandler handler = new ContextAwareConfigurationMethodInvocationHandler(
                repo, new ConditionsProcessor(), retriever);

        Method method = Express.class.getMethod("stateDefault");
        Object object = handler.invoke("", method, new String[]{});
        assertThat(object, Matchers.equalTo(State.SHIPPING));
    }

    @Test
    public void testPrimitiveType() throws Throwable {

        final ConfigurationRepository repo = mock(ConfigurationRepository.class);
        final Configuration<String> configuration = new Configuration<>(
                "express.max.delivery.days",
                "This is a test configuration object.", ImmutableSet.of(), "3");
        when(repo.get(anyString())).thenReturn(Optional.of(configuration));

        final ContextProviderRetriever retriever = mock(
                ContextProviderRetriever.class,
                org.mockito.Answers.RETURNS_SMART_NULLS.toString());

        ContextAwareConfigurationMethodInvocationHandler handler = new ContextAwareConfigurationMethodInvocationHandler(
                repo, new ConditionsProcessor(), retriever);

        Method method = Express.class.getMethod("maxDeliveryDays");
        Object object = handler.invoke("", method, new String[]{});
        assertThat(object, Matchers.equalTo(3));
    }

}