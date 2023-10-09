package org.zalando.baigan.proxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class ConfigTypeProvider {

    private final BaiganConfigClasses baiganConfigClasses;

    @Autowired
    ConfigTypeProvider(BaiganConfigClasses baiganConfigClasses) {
        this.baiganConfigClasses = baiganConfigClasses;
    }

    public Type getType(final String configKey) {
        return baiganConfigClasses.getConfigTypesByKey().get(configKey);
    }
}
