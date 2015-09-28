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

import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.common.collect.ImmutableSet;

@RunWith(JUnit4.class)
public class TestEndsWith {

    @Test
    public void testEndsWithEval() {
        final String bar = "foo@bar.com";
        final String gmx = "hello@gmx.de";
        final String bazinga = "baz@bazinga.com";
        final String sally = "sally@zalando.uk";
        final ConditionType conditionType = new EndsWith(
                ImmutableSet.of("gmx.de", "zalando.uk"));

        assertThat(conditionType.eval(bar), Matchers.equalTo(false));
        assertThat(conditionType.eval(gmx), Matchers.equalTo(true));
        assertThat(conditionType.eval(bazinga), Matchers.equalTo(false));
        assertThat(conditionType.eval(sally), Matchers.equalTo(true));

    }
}
