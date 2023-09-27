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

import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Implementatione if the context param
 *
 * @author mchand
 *
 */
public class EndsWith extends ConditionType {

    private static final long serialVersionUID = 5346539855029708345L;

    @JsonProperty("endsWithValue")
    private final Set<String> endsWithValue;

    @JsonCreator
    public EndsWith(
            @JsonProperty("endsWithValue") final Set<String> endsWithValue) {
        this.endsWithValue = endsWithValue;
    }

    @Override
    public boolean eval(final String forValue) {
        for (final String endsWith : endsWithValue) {
            if (StringUtils.endsWithIgnoreCase(forValue, endsWith)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndsWith endsWith = (EndsWith) o;
        return Objects.equals(endsWithValue, endsWith.endsWithValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endsWithValue);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EndsWith.class.getSimpleName() + "[", "]")
                .add("endsWithValue=" + endsWithValue)
                .toString();
    }
}
