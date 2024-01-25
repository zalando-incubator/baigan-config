/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *         http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zalando.baigan.proxy.handler;

import java.util.Map;

import javax.annotation.Nonnull;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.zalando.baigan.model.Condition;
import org.zalando.baigan.model.Configuration;

/**
 * @author mchand
 */

@Component
public class ConditionsProcessor {

    @Nonnull
    public <T> T process(Configuration<T> configuration,
            Map<String, String> context) {

        T value = configuration.getDefaultValue();

        if (CollectionUtils.isEmpty(configuration.getConditions())) {
            return value;
        }

        for (Condition<T> condition : configuration.getConditions()) {

            final String conditionalContextParamValue = context
                    .get(condition.getParamName());
            final boolean result = condition.getConditionType()
                    .eval(conditionalContextParamValue);

            // Return if any of the condition evaluates to true from the ordered
            // set of conditions.
            if (result) {
                return condition.getValue();
            }
        }
        return value;
    }
}
