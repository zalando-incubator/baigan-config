package org.zalando.baigan.proxy.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.zalando.baigan.context.ContextProviderRetriever;
import org.zalando.baigan.model.Configuration;
import org.zalando.baigan.context.ContextProvider;
import org.zalando.baigan.repository.ConfigurationRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContextAwareConfigurationMethodInvocationHandlerTest {

    private static final String key = "test.interface.get.some.value";
    private static final String expectedConfigValue = "some value";
    private static final Configuration<String> config = new Configuration<>(key, "description", Set.of(), expectedConfigValue);

    private final ConfigurationRepository repository = mock(ConfigurationRepository.class);
    private final ConditionsProcessor conditionsProcessor = mock(ConditionsProcessor.class);
    private final ContextProviderRetriever contextProviderRetriever = mock(ContextProviderRetriever.class);
    private final BeanFactory beanFactory = mock(BeanFactory.class);
    private final ContextAwareConfigurationMethodInvocationHandler handler = new ContextAwareConfigurationMethodInvocationHandler();

    @BeforeEach
    public void setup() {
        when(beanFactory.getBean(ConfigurationRepository.class)).thenReturn(repository);
        when(beanFactory.getBean(ConditionsProcessor.class)).thenReturn(conditionsProcessor);
        when(beanFactory.getBean(ContextProviderRetriever.class)).thenReturn(contextProviderRetriever);
        handler.setBeanFactory(beanFactory);
    }

    @Test
    public void whenConfigurationRepositoryReturnsEmpty_shouldReturnNull() {
        when(repository.get(key)).thenReturn(Optional.empty());
        final Object result = handler.handleInvocation((TestInterface) () -> null, TestInterface.class.getDeclaredMethods()[0], new Object[0]);
        assertThat(result, nullValue());
    }

    @Test
    public void whenContextIsEmpty_shouldReturnValueFromConditionsProcessorCalledWithEmptyContext() {
        final Configuration<Integer> configWithWrongType = new Configuration<>(key, "description", Set.of(), 1);
        when(repository.get(key)).thenReturn(Optional.of(configWithWrongType));
        when(contextProviderRetriever.getContextParameterKeys()).thenReturn(Set.of());
        when(conditionsProcessor.process(configWithWrongType, Map.of())).thenReturn(1);
        final Object result = handler.handleInvocation((TestInterface) () -> null, TestInterface.class.getDeclaredMethods()[0], new Object[0]);
        assertThat(result, nullValue());
    }

    @Test
    public void whenConfigReturnTypeDoesNotMatchMethodReturnType_shouldReturnNull() {
        when(repository.get(key)).thenReturn(Optional.of(config));
        when(contextProviderRetriever.getContextParameterKeys()).thenReturn(Set.of());
        when(conditionsProcessor.process(config, Map.of())).thenReturn(expectedConfigValue);
        final Object result = handler.handleInvocation((TestInterface) () -> null, TestInterface.class.getDeclaredMethods()[0], new Object[0]);
        assertThat(result, equalTo(expectedConfigValue));
    }

    @Test
    public void whenContextProvidersExist_shouldReturnValueFromConditionsProcessorCalledWithResultingContext() {
        when(repository.get(key)).thenReturn(Optional.of(config));

        final String param1 = "param1";
        final String param2 = "param2";
        when(contextProviderRetriever.getContextParameterKeys()).thenReturn(Set.of(param1, param2));

        final ContextProvider contextProvider1 = mock(ContextProvider.class);
        when(contextProvider1.getContextParam(param1)).thenReturn("value1");
        when(contextProviderRetriever.getProvidersFor(param1)).thenReturn(Set.of(contextProvider1));

        final ContextProvider contextProvider2 = mock(ContextProvider.class);
        when(contextProvider2.getContextParam(param2)).thenReturn("value2");
        when(contextProviderRetriever.getProvidersFor(param2)).thenReturn(Set.of(contextProvider2));

        when(conditionsProcessor.process(config, Map.of(param1, "value1", param2, "value2"))).thenReturn(expectedConfigValue);
        final Object result = handler.handleInvocation((TestInterface) () -> null, TestInterface.class.getDeclaredMethods()[0], new Object[0]);
        assertThat(result, equalTo(expectedConfigValue));
    }

    @Test
    public void whenContextProvidersAreEmpty_shouldReturnValueFromConditionsProcessorCalledWithEmptyContext() {
        when(repository.get(key)).thenReturn(Optional.of(config));

        final String param = "param";
        when(contextProviderRetriever.getContextParameterKeys()).thenReturn(Set.of(param));
        when(contextProviderRetriever.getProvidersFor(param)).thenReturn(List.of());

        when(conditionsProcessor.process(config, Map.of())).thenReturn(expectedConfigValue);
        final Object result = handler.handleInvocation((TestInterface) () -> null, TestInterface.class.getDeclaredMethods()[0], new Object[0]);
        assertThat(result, equalTo(expectedConfigValue));
    }


    // FIXME this behavior seems strange
    @Test
    public void whenThereAreMultipleContextProvidersForOneParam_shouldConsiderOnlyTheFirstContextProvider() {
        when(repository.get(key)).thenReturn(Optional.of(config));

        final String param = "param";
        when(contextProviderRetriever.getContextParameterKeys()).thenReturn(Set.of(param));

        final ContextProvider contextProvider1 = mock(ContextProvider.class);
        when(contextProvider1.getContextParam(param)).thenReturn("value1");

        final ContextProvider contextProvider2 = mock(ContextProvider.class);
        when(contextProvider2.getContextParam("some ignored param")).thenReturn("some ignored value");
        when(contextProviderRetriever.getProvidersFor(param)).thenReturn(List.of(contextProvider1, contextProvider2));

        when(conditionsProcessor.process(config, Map.of(param, "value1"))).thenReturn(expectedConfigValue);
        final Object result = handler.handleInvocation((TestInterface) () -> null, TestInterface.class.getDeclaredMethods()[0], new Object[0]);
        assertThat(result, equalTo(expectedConfigValue));
    }

    interface TestInterface {
        String getSomeValue();
    }
}
