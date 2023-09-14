package org.zalando.baigan.proxy;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BaiganConfigClasses {
    private Map<String, Class<?>> configTypesByKey;

    public BaiganConfigClasses() {}

    public BaiganConfigClasses(Map<String, Class<?>> configTypesByKey) {
        this.configTypesByKey = configTypesByKey;
    }

    public void setConfigTypesByKey(Map<String, Class<?>> configTypesByKey) {
        this.configTypesByKey = configTypesByKey;
    }

    public Map<String, Class<?>> getConfigTypesByKey() {
        return configTypesByKey;
    }
}
