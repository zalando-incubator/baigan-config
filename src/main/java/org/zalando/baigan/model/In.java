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

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Implementation of ConditionType that evaluates to true if the context param
 * matches the configured value..
 *
 * @author mchand
 *
 */
public class In extends ConditionType {

    private static final long serialVersionUID = 5346539855029708345L;

    @JsonProperty("inValue")
    private final Set<String> inValue;

    @JsonCreator
    public In(@JsonProperty("inValue") final Set<String> inValue) {
        this.inValue = inValue;
    }

    @Override
    public boolean eval(final String forValue) {
        return inValue.contains(forValue);
    }

}
