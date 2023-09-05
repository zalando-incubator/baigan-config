package org.zalando.baigan.context;

import com.google.common.collect.ImmutableSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.baigan.model.Condition;
import org.zalando.baigan.service.ConfigurationRepository;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;

/**
 * @author mchand
 */
@Configuration
public class SpringTestContext {

    @Bean
    public ConfigurationRepository configurationRepository() {
        return new ConfigurationRepository() {
            final static String KEY = "test.config.enable.xyz.feature";

            public void put(@Nonnull String key, @Nonnull String value) {
            }

            @Nonnull
            public Optional<org.zalando.baigan.model.Configuration> get(@Nonnull String key) {
                if (KEY.equalsIgnoreCase(key)) {
                    return Optional.of(mockConfiguration(key));
                } else {
                    return Optional.empty();
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
