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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * Implementation of ConditionType that evaluates to true if the context param
 * matches the configured value.
 *
 * @author mchand
 *
 */
public class Equals extends ConditionType {

    private static final long serialVersionUID = 5346539855029708345L;

    @JsonProperty("onValue")
    private final String onValue;

    @JsonCreator
    public Equals(@JsonProperty("onValue") final String onValue) {
        this.onValue = onValue;
    }

    @Override
    public boolean eval(final String forValue) {
        return onValue.equalsIgnoreCase(forValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equals equals = (Equals) o;
        return Objects.equals(onValue, equals.onValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(onValue);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Equals.class.getSimpleName() + "[", "]")
                .add("onValue='" + onValue + "'")
                .toString();
    }
}
