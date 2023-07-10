package org.zalando.baigan;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.zalando.baigan.annotation.ConfigurationServiceScan;
import org.zalando.baigan.fixture.SomeConfiguration;
import org.zalando.baigan.service.ConfigurationRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AnnotationBeanRegistrationIT.TestContext.class})
public class AnnotationBeanRegistrationIT {

    @Configuration
    @ComponentScan(basePackageClasses = BaiganSpringContext.class)
    @ConfigurationServiceScan(basePackages = "org.zalando.baigan.fixture")
    static class TestContext {
        @Bean
        ConfigurationRepository configurationRepository() {
            return mock(ConfigurationRepository.class);
        }
    }

    @Autowired
    private ApplicationContext context;

    @Test
    void shouldRegisterAnnotatedConfigurationAsBean() {
        final SomeConfiguration myBean = context.getBean(SomeConfiguration.class);
        assertThat(myBean, is(not(nullValue())));
    }
}
