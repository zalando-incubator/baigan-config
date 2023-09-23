package org.zalando.baigan.e2e.s3repo;

import org.zalando.baigan.annotation.BaiganConfig;

@BaiganConfig
public interface SomeConfiguration {
    SomeConfigObject someConfig();
    String someValue();
    Boolean isThisTrue();
}
