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

package org.zalando.baigan.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.baigan.model.Condition;
import org.zalando.baigan.model.Configuration;
import org.zalando.baigan.model.Equals;
import org.zalando.baigan.proxy.handler.ConditionsProcessor;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TestEtcdConfigService {

    private final static ObjectMapper mapper = new ObjectMapper()
            .registerModule(new GuavaModule());

    private Configuration<Boolean> configuration;

    private String buffer = null;

    @BeforeEach
    public void init() throws IOException {

        final Condition<Boolean> condition = new Condition<>("appdomain",
                new Equals("1"), true);

        final Set<Condition<Boolean>> conditions = ImmutableSet.of(condition);
        configuration = new Configuration("express.feature.toggle",
                "Feature toggle", conditions, Boolean.FALSE);

        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, configuration);
        buffer = writer.toString();
    }

    private void testConfiguration(final Configuration<Boolean> configuration) {

        final ConditionsProcessor conditionsProcessor = new ConditionsProcessor();

        assertThat(conditionsProcessor.process(configuration,
                ImmutableMap.of("appdomain", "1")), equalTo(true));

        assertThat(conditionsProcessor.process(configuration,
                ImmutableMap.of("appdomain", "2")), equalTo(false));
    }

    @Test
    public void testBooleanConfiguration() {
        testConfiguration(configuration);
    }

    @Test
    public void testDeserialize() throws Exception {
        final Configuration deserializedConfiguration = mapper.readValue(buffer,
                Configuration.class);
        testConfiguration(deserializedConfiguration);
    }

}
