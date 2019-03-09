package org.zalando.baigan.spring.autoconfigure;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

// TODO: generate ConfigurationStore bean via application properties
@Configuration
@Import(ConfigurationBeanAutoConfigureRegistrar.class)
public class BaiganAutoConfiguration {

}
