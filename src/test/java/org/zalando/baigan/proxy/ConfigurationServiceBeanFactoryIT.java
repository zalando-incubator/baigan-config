package org.zalando.baigan.proxy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.zalando.baigan.BaiganSpringContext;
import org.zalando.baigan.annotation.BaiganConfig;
import org.zalando.baigan.annotation.ConfigurationServiceScan;
import org.zalando.baigan.service.ConfigurationRepository;
import org.zalando.baigan.service.github.GitConfig;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ConfigurationServiceBeanFactoryIT.TestContext.class})
/*
 * The purpose of this test is to prove that BeanPostProcessor and BeanFactoryPostProcessor are actually executed.
 * This is to ensure that Baigan does not silently break annotations like @Cachable or @Traced,
 * which had happened before.
 */
public class ConfigurationServiceBeanFactoryIT {

    @ComponentScan(basePackageClasses = {BaiganSpringContext.class})
    @ConfigurationServiceScan(basePackages = "org.zalando.baigan.proxy")
    static class TestContext {

        @Bean
        ConfigurationRepository configurationRepository(final GitConfig configuration) {
            return mock(ConfigurationRepository.class);
        }

        @Bean(name = "gitConfiguration")
        GitConfig gitConfiguration() {
            final GitConfig config = mock(GitConfig.class);
            when(config.getGitHost()).thenReturn("raw.com");
            return config;
        }

        @Bean
        static TestPostProcessor testPostProcessor() {
            return new TestPostProcessor();
        }

        @Bean
        static TestBeanFactoryPostProcessor testBeanFactoryPostProcessor() {
            return new TestBeanFactoryPostProcessor();
        }

    }

    static class TestPostProcessor implements BeanPostProcessor {

        @Override
        public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
            if ("gitConfiguration".equals(beanName)) {
                when(((GitConfig) bean).getGitHost()).thenReturn("post-processed.com");
            }
            return bean;
        }
    }

    static class MyDependency {

    }

    static class TestBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

        @Override
        public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
            // force instantiation of factory beans
            beanFactory.getBeanNamesForType(MyDependency.class);

            // proof that post processor actually runs
            beanFactory.registerSingleton("myBean", new MyDependency());
        }
    }

    @BaiganConfig
    public interface TestFeature {

        Boolean enabled();
    }

    @Autowired
    private GitConfig gitConfig;

    @Autowired
    private MyDependency myDependency;

    @Test
    public void allowsPostProcessingOfBeans() {
        assertThat(gitConfig.getGitHost(), is("post-processed.com"));
    }

    @Test
    public void allowsPostProcessingOfFactoryBeans() {
        // A bean of type MyDependency exists only because it is registered in TestBeanFactoryPostProcessor.
        // It's existence proves that the post processor is running.
        assertThat(myDependency, is(notNullValue()));
    }
}
