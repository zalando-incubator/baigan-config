package org.zalando.baigan.e2e.configs;

import org.zalando.baigan.annotation.BaiganConfig;

@BaiganConfig
public interface SomeConfiguration {
    String someValue();
    Boolean isThisTrue();
}
