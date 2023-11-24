package org.zalando.baigan.proxy;

import java.lang.reflect.Type;
import java.util.Map;

public class BaiganConfigClasses {
    private Map<String, Type> configTypesByKey;

    public BaiganConfigClasses() {}

    public void setConfigTypesByKey(Map<String, Type> configTypesByKey) {
        configTypesByKey.forEach((key, value) -> {
            if (value.getClass().isPrimitive()) {
                throw new IllegalArgumentException("Config " + key + " has an illegal return type " + value + ". Primitives are not supported as return type.");
            }
        });
        this.configTypesByKey = configTypesByKey;
    }

    public Map<String, Type> getConfigTypesByKey() {
        return configTypesByKey;
    }
}
