package org.zalando.baigan.proxy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.baigan.BaiganSpringContext;
import org.zalando.baigan.annotation.BaiganConfig;
import org.zalando.baigan.annotation.ConfigurationServiceScan;
import org.zalando.baigan.context.SpringTestContext;
import org.zalando.baigan.service.ConfigurationRepository;
import org.zalando.baigan.service.github.GitConfig;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ConfigurationServiceBeanFactoryIT.TestContext.class})
public class ConfigurationServiceBeanFactoryIT {

    @Configuration
    @ComponentScan(
            basePackageClasses = {BaiganSpringContext.class},
            excludeFilters = @ComponentScan.Filter(
                    classes = SpringTestContext.class,
                    type = FilterType.ASSIGNABLE_TYPE))
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

    @BaiganConfig
    public interface TestFeature {

        Boolean enabled();
    }

    @Autowired
    private GitConfig gitConfig;

    @Test
    public void allowsPostProcessingOfBeans() throws Exception {
        assertThat(gitConfig.getGitHost(), is("post-processed.com"));
    }
}
