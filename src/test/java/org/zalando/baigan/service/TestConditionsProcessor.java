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

package org.zalando.baigan.service;

import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.zalando.baigan.model.Condition;
import org.zalando.baigan.model.Configuration;
import org.zalando.baigan.model.EndsWith;
import org.zalando.baigan.model.In;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

@RunWith(JUnit4.class)
public class TestConditionsProcessor {

    private static final String NONE = "NONE";
    private static final String DHL = "DHL";
    private static final String CUSTOMER_NUMBER = "customerNumber";
    private static final String APPDOMAIN = "appdomain";

    @Test
    public void testEndsWithEval() {

        final ConditionsProcessor processor = new ConditionsProcessor();

        final Configuration<Boolean> configuration = createMultiConditionsForInAppdomainOrCustomerNumberEndsWith(
                ImmutableSet.of("1", "3"), ImmutableSet.of("1", "2"));

        final Map<String, String> context1 = ImmutableMap.of(APPDOMAIN, "1",
                CUSTOMER_NUMBER, "1239");
        assertThat(processor.process(configuration, context1),
                Matchers.equalTo(true));

        final Map<String, String> context2 = ImmutableMap.of(APPDOMAIN, "4",
                CUSTOMER_NUMBER, "5712");
        assertThat(processor.process(configuration, context2),
                Matchers.equalTo(true));

        final Map<String, String> context3 = ImmutableMap.of(APPDOMAIN, "6",
                CUSTOMER_NUMBER, "5718");
        assertThat(processor.process(configuration, context3),
                Matchers.equalTo(false));

    }

    @Test
    public void testConfigurationValueStringType() {

        final ConditionsProcessor processor = new ConditionsProcessor();

        final Configuration<String> configuration = createConditionsForInAppdomain(
                ImmutableSet.of("1", "3"));

        final Map<String, String> context1 = ImmutableMap.of(APPDOMAIN, "1");
        assertThat(processor.process(configuration, context1),
                Matchers.equalTo(DHL));

        final Map<String, String> context2 = ImmutableMap.of(APPDOMAIN, "4");
        assertThat(processor.process(configuration, context2),
                Matchers.equalTo(NONE));

    }

    private Configuration<String> createConditionsForInAppdomain(
            final Set<String> appdomain) {
        final Condition<String> conditionForAppdomains = new Condition<String>(
                APPDOMAIN, new In(appdomain), DHL);

        final Set<Condition<String>> conditions = ImmutableSet
                .of(conditionForAppdomains);

        return new Configuration<String>("express.service.provider",
                "Express service provider", conditions, NONE);
    }

    private Configuration<Boolean> createMultiConditionsForInAppdomainOrCustomerNumberEndsWith(
            final Set<String> appdomain, final Set<String> customerNumbers) {
        final Condition<Boolean> conditionForAppdomains = new Condition<Boolean>(
                APPDOMAIN, new In(appdomain), true);

        final Condition<Boolean> conditionForCustomerNumber = new Condition<Boolean>(
                CUSTOMER_NUMBER, new EndsWith(customerNumbers), true);

        final Set<Condition<Boolean>> conditions = ImmutableSet
                .of(conditionForAppdomains, conditionForCustomerNumber);

        return new Configuration<Boolean>("express.feature.toggle",
                "Feature toggle", conditions, Boolean.FALSE);
    }
}
