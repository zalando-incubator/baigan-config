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

package org.zalando.baigan;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.zalando.baigan.model.Condition;
import org.zalando.baigan.model.Configuration;
import org.zalando.baigan.model.Equals;
import org.zalando.baigan.service.ConditionsProcessor;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

@RunWith(JUnit4.class)
public class TestConfigurationSerialization {

    private ObjectMapper mapper;

    private ConditionsProcessor conditionsProcessor;

    private final String stringy = "{\"alias\":\"express.feature.toggle\",\"description\":\"Feature toggle\",\"conditions\":"
            + "[{\"paramName\":\"appdomain\",\"conditionType\":{\"type\":\"Equals\",\"onValue\":\"1\"},\"value\":true}],\"defaultValue\":false}";

    @Before
    public void init()
            throws JsonMappingException, JsonGenerationException, IOException {
        mapper = new ObjectMapper().registerModule(new GuavaModule());
        conditionsProcessor = new ConditionsProcessor();

    }

    private Configuration createConfigurationForAppdomain1() {
        final Condition<Boolean> condition = new Condition<Boolean>("appdomain",
                new Equals("1"), true);
        final Set<Condition<Boolean>> conditions = ImmutableSet.of(condition);
        final Configuration configuration = new Configuration(
                "express.feature.toggle", "Feature toggle", conditions,
                Boolean.FALSE);
        return configuration;
    }

    private void testConfigurationTrueOnlyForAppdomain1(
            final Configuration<Boolean> paramConfiguration) {
        assertTrue(conditionsProcessor.process(paramConfiguration,
                ImmutableMap.of("appdomain", "1")));

        assertFalse(conditionsProcessor.process(paramConfiguration,
                ImmutableMap.of("appdomain", "2")));
    }

    @Test
    public void testBooleanConfiguration() throws Exception {
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
        assertThat(serialized, Matchers.equalTo(stringy));

    }

}
