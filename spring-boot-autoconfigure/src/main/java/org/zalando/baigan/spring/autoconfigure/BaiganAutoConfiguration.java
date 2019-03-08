package org.zalando.baigan.spring.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.zalando.baigan.ConfigurationStore;
import java.util.Optional;

@Configuration
@Import(ConfigurationBeanAutoConfigureRegistrar.class)
public class BaiganAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConfigurationStore baiganConfigurationStore() {
        return new ConfigurationStore() {
            @Override
            public <T> Optional<org.zalando.baigan.Configuration> getConfiguration(final String namespace, final String key) {
                return Optional.empty();
            }
        };
    }

}
