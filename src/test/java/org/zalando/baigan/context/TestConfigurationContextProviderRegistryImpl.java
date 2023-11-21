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

package org.zalando.baigan.context;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 *
 * @author mchand
 *
 */
@ExtendWith(MockitoExtension.class)
public class TestConfigurationContextProviderRegistryImpl {

    final String TEST_CONTEXT_PARAM_VALUE = "99";

    final String CONTEXT_PARAM = ConfigurationContext.APPDOMAIN.name();

    @Test
    public void testRegister() {
        final ConfigurationContextProviderRegistryImpl registry = new ConfigurationContextProviderRegistryImpl();

        ContextProvider providerMock = Mockito.mock(ContextProvider.class);
        Mockito.when(providerMock.getProvidedContexts()).thenReturn(
                ImmutableSet.of(ConfigurationContext.APPDOMAIN.name()));
        Mockito.when(providerMock.getContextParam(CONTEXT_PARAM))
                .thenReturn(TEST_CONTEXT_PARAM_VALUE);

        registry.register(providerMock);

        final Collection<ContextProvider> providers = registry
                .getProvidersFor(ConfigurationContext.APPDOMAIN.name());
        assertThat(providers.size(), equalTo(1));
        final ContextProvider provider = providers.iterator().next();

        final String contextParamValue = provider
                .getContextParam(ConfigurationContext.APPDOMAIN.name());
        assertThat(contextParamValue, equalTo(TEST_CONTEXT_PARAM_VALUE));

    }
}
