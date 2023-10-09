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
import java.util.Objects;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * Represents a rule/condition that if met
 *
 * @param <T>
 *
 * @author mchand
 */
public class Condition<T> implements Serializable {

    private static final long serialVersionUID = -7919202855689561277L;

    @JsonProperty
    private String paramName;

    @JsonProperty
    private ConditionType conditionType;

    @JsonProperty
    private T value;

    @JsonCreator
    public Condition(@JsonProperty("paramName") final String paramName,
            @JsonProperty("conditionType") ConditionType conditionType,
            @JsonProperty("value") T value) {
        this.paramName = paramName;
        this.conditionType = conditionType;
        this.value = value;
    }

    public String getParamName() {
        return paramName;
    }

    public T getValue() {
        return value;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Condition<?> condition = (Condition<?>) o;
        return Objects.equals(paramName, condition.paramName) && Objects.equals(conditionType, condition.conditionType) && Objects.equals(value, condition.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paramName, conditionType, value);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Condition.class.getSimpleName() + "[", "]")
                .add("paramName='" + paramName + "'")
                .add("conditionType=" + conditionType)
                .add("value=" + value)
                .toString();
    }
}
