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

package org.zalando.baigan.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.baigan.proxy.handler.ConditionsProcessor;

import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestConfigurationSerialization {

    private ObjectMapper mapper;

    private ConditionsProcessor conditionsProcessor;

    private final String stringy = "{\"alias\":\"express.feature.toggle\",\"description\":\"Feature toggle\",\"conditions\":"
            + "[{\"paramName\":\"appdomain\",\"conditionType\":{\"type\":\"Equals\",\"onValue\":\"1\"},\"value\":true}],\"defaultValue\":false}";

    @BeforeEach
    public void init() {
        mapper = new ObjectMapper().registerModule(new GuavaModule());
        conditionsProcessor = new ConditionsProcessor();

    }

    private Configuration createConfigurationForAppdomain1() {
        final Condition<Boolean> condition = new Condition<>("appdomain",
                new Equals("1"), true);
        final Set<Condition<Boolean>> conditions = ImmutableSet.of(condition);
        final Configuration configuration = new Configuration(
                "express.feature.toggle", "Feature toggle", conditions,
                Boolean.FALSE);
        return configuration;
    }

    private void testConfigurationTrueOnlyForAppdomain1(
            final Configuration<Boolean> paramConfiguration) {
        assertThat(conditionsProcessor.process(paramConfiguration,
                ImmutableMap.of("appdomain", "1")), equalTo(true));

        assertThat(conditionsProcessor.process(paramConfiguration,
                ImmutableMap.of("appdomain", "2")), equalTo(false));
    }

    @Test
    public void testBooleanConfiguration() {
        testConfigurationTrueOnlyForAppdomain1(
                createConfigurationForAppdomain1());
    }

    @Test
    public void testDeserialize() throws Exception {

        final Configuration deserializedConfiguration = mapper
                .readValue(stringy, Configuration.class);
        testConfigurationTrueOnlyForAppdomain1(deserializedConfiguration);

    }

    @Test
    public void testSerialize() throws Exception {
        final Configuration config = createConfigurationForAppdomain1();
        final String serialized = mapper.writeValueAsString(config);
        assertThat(serialized, equalTo(stringy));
    }

}
