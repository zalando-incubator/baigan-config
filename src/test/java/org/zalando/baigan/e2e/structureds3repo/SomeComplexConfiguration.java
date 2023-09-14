package org.zalando.baigan.e2e.structureds3repo;

import org.zalando.baigan.annotation.BaiganConfig;

@BaiganConfig
public interface SomeComplexConfiguration {
    SomeConfigObject someConfig();
    SomeOtherConfigObject someOtherConfig();
}
