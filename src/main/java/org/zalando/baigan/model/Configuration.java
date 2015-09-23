/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zalando.baigan.model;

import java.io.Serializable;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author mchand
 */

public class Configuration<Type> implements Serializable {

    private static final long serialVersionUID = 9051980436564855711L;

    @JsonProperty
    private String alias;

    @JsonProperty
    private String description;

    @JsonProperty
    private Set<Condition<Type>> conditions;

    @JsonProperty
    private Type defaultValue;

    public Configuration(@JsonProperty("alias") final String alias,
            @JsonProperty("description") final String description,
            @JsonProperty("conditions") final Set<Condition<Type>> conditions,
            @JsonProperty("defaultValue") final Type defaultValue) {
        this.alias = alias;
        this.description = description;
        this.conditions = conditions;
        this.defaultValue = defaultValue;
    }

    public String getAlias() {
        return alias;
    }

    public String getDescription() {
        return description;
    }

    public Set<Condition<Type>> getConditions() {
        return conditions;
    }

    public Type getDefaultValue() {
        return defaultValue;
    }

}
