package org.zalando.baigan.e2e.configs;

import org.zalando.baigan.annotation.BaiganConfig;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@BaiganConfig
public interface SomeConfiguration {
    SomeConfigObject someConfig();
    String someValue();
    Boolean isThisTrue();
    Map<UUID, List<SomeConfigObject>> topLevelGenerics();
}
