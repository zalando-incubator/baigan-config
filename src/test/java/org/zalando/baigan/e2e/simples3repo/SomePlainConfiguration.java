package org.zalando.baigan.e2e.simples3repo;

import org.zalando.baigan.annotation.BaiganConfig;

@BaiganConfig
public interface SomePlainConfiguration {
    String someValue();
    Boolean isThisTrue();
}
