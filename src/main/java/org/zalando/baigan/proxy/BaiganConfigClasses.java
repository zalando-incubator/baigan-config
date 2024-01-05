package org.zalando.baigan.proxy;

import java.lang.reflect.Type;
import java.util.Map;

public class BaiganConfigClasses {
    private Map<String, Type> configTypesByKey;

    public BaiganConfigClasses() {}

    public void setConfigTypesByKey(Map<String, Type> configTypesByKey) {
        this.configTypesByKey = configTypesByKey;
    }

    public Map<String, Type> getConfigTypesByKey() {
        return configTypesByKey;
    }
}
