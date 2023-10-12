package org.zalando.baigan.e2e.configs;

import org.zalando.baigan.annotation.BaiganConfig;

import java.util.List;

@BaiganConfig
public interface SomeConfiguration {
    String someValue();
    Boolean isThisTrue();
    List<String> configList();
}
