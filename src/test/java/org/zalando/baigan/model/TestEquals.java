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

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestEquals {

    @Test
    public void testEval() {
        final String email = "foo@bar.com";
        final String otherCase = "FOo@bar.com";
        final String other = "baz@bar.com";
        final ConditionType conditionType = new Equals(email);

        assertThat(conditionType.eval(other), equalTo(false));
        assertThat(conditionType.eval(email), equalTo(true));

        assertThat(conditionType.eval(otherCase), equalTo(true));

    }
}
