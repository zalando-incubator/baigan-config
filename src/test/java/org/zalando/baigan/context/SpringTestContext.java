package org.zalando.baigan.context;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.baigan.model.Condition;
import org.zalando.baigan.service.ConditionsProcessor;
import org.zalando.baigan.service.ConfigurationRepository;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * @author mchand
 */
@Configuration
class SpringTestContext {

    @Bean
    public ConditionsProcessor ConditionsProcessor() {
        return new ConditionsProcessor();
    }

    @Bean
    public ConfigurationRepository configurationRepository() {
        return new ConfigurationRepository() {
            final static String KEY = "test.config.enable.xyz.feature";

            public void put(@Nonnull String key, @Nonnull String value) {
            }

            @Nonnull
            public Optional<org.zalando.baigan.model.Configuration<?>> getConfig(@Nonnull String key) {
                if (KEY.equalsIgnoreCase(key)) {
                    return Optional.of(mockConfiguration(key));
                } else {
                    return Optional.absent();
                }

            }

            private org.zalando.baigan.model.Configuration<Boolean> mockConfiguration(
                    final String key) {

                final Set<Condition<Boolean>> conditions = ImmutableSet.of();

                return new org.zalando.baigan.model.Configuration<>(
                        key, "This is a test configuration object.", conditions,
                        Boolean.FALSE);
            }
        };
    }
}
