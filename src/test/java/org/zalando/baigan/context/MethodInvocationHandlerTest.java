package org.zalando.baigan.context;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.beans.factory.BeanFactory;
import org.zalando.baigan.model.Configuration;
import org.zalando.baigan.proxy.handler.ContextAwareConfigurationMethodInvocationHandler;
import org.zalando.baigan.service.ConditionsProcessor;
import org.zalando.baigan.service.ConfigurationRepository;

import java.lang.reflect.InvocationHandler;
import java.util.Optional;

import static com.google.common.collect.ImmutableSet.of;
import static com.google.common.reflect.Reflection.newProxy;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

enum State {
    SHIPPING, SHIPPED, DELIVERED
}

interface Base {

    boolean isActive();

}

interface Express extends Base {

    State stateDefault();

    int maxDeliveryDays();
}

/**
 * @author mchand
 */
@RunWith(JUnit4.class)
public class MethodInvocationHandlerTest {

    private static final String DESCRIPTION = "This is a test configuration object.";

    @Test
    public void testEnumValue() throws Throwable {

        final ConfigurationRepository repo = mock(ConfigurationRepository.class);
        final Configuration<String> configuration = new Configuration<>("express.state.default", DESCRIPTION, of(), "SHIPPING");
        when(repo.get(anyString())).thenReturn(Optional.of(configuration));

        final ContextAwareConfigurationMethodInvocationHandler handler = createHandler(repo);

        Object object = invokeHandler(handler, Express.class, "stateDefault");
        assertThat(object, Matchers.equalTo(State.SHIPPING));
    }

    @Test
    public void testIllegalEnumValue() throws Throwable {

        final ConfigurationRepository repo = mock(ConfigurationRepository.class);
        final Configuration<String> configuration = new Configuration<>("express.state.default", DESCRIPTION, of(), "42");
        when(repo.get(anyString())).thenReturn(Optional.of(configuration));

        final ContextAwareConfigurationMethodInvocationHandler handler = createHandler(repo);

        Object object = invokeHandler(handler, Express.class, "stateDefault");
        assertThat(object, is(nullValue()));
    }

    @Test
    public void testPrimitiveType() throws Throwable {

        final ConfigurationRepository repo = mock(ConfigurationRepository.class);
        final Configuration<String> configuration = new Configuration<>("express.max.delivery.days", DESCRIPTION, of(), "3");
        when(repo.get(anyString())).thenReturn(Optional.of(configuration));

        final ContextAwareConfigurationMethodInvocationHandler handler = createHandler(repo);

        Object object = invokeHandler(handler, Express.class, "maxDeliveryDays");
        assertThat(object, Matchers.equalTo(3));
    }

    @Test
    public void testInheritedToggle() throws Throwable {

        final ConfigurationRepository repo = mock(ConfigurationRepository.class);
        final Configuration<String> configuration = new Configuration<>("express.is.active", DESCRIPTION, of(), Boolean.TRUE.toString());
        when(repo.get(anyString())).thenReturn(Optional.of(configuration));

        final ContextAwareConfigurationMethodInvocationHandler handler = createHandler(repo);

        Object object = invokeHandler(handler, Express.class, "isActive");
        assertThat(object, is(true));
    }

    private ContextAwareConfigurationMethodInvocationHandler createHandler(final ConfigurationRepository repository) {
        final ContextProviderRetriever retriever = mock(
                ContextProviderRetriever.class,
                org.mockito.Answers.RETURNS_SMART_NULLS.toString());

        final BeanFactory beanFactory = mock(BeanFactory.class);
        when(beanFactory.getBean(ContextProviderRetriever.class)).thenReturn(retriever);
        when(beanFactory.getBean(ConfigurationRepository.class)).thenReturn(repository);
        when(beanFactory.getBean(ConditionsProcessor.class)).thenReturn(new ConditionsProcessor());

        final ContextAwareConfigurationMethodInvocationHandler handler =
                new ContextAwareConfigurationMethodInvocationHandler();
        handler.setBeanFactory(beanFactory);
        return handler;
    }

    private Object invokeHandler(final InvocationHandler handler, final Class<?> handlerInterface, final String methodName) throws Throwable {
        return handler.invoke(newProxy(handlerInterface, handler), handlerInterface.getMethod(methodName), new String[]{});
    }

}