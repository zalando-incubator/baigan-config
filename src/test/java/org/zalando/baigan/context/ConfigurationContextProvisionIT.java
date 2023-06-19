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

package org.zalando.baigan.context;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.zalando.baigan.context.ConfigurationContextProvisionIT.StaticAppdomainProvider;
import org.zalando.baigan.provider.ContextProvider;

import java.util.Collection;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Mohammed Amjed
 *
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { StaticAppdomainProvider.class,
        ConfigurationContextProviderRegistryImpl.class,
        ConfigurationContextProviderBeanPostprocessor.class })
public class ConfigurationContextProvisionIT {

    @Autowired
    private ContextProviderRetriever retriever;

    @Test
    public void testProvisioning() {

        final String contextParam = ConfigurationContext.APPDOMAIN.name();
        final Collection<ContextProvider> contextProviders = retriever
                .getProvidersFor(contextParam);

        assertThat(contextProviders.size(), equalTo(1));

        final String contextValue = contextProviders.iterator().next()
                .getContextParam(contextParam);
        assertThat(contextValue, equalTo("1"));
    }

    static class StaticAppdomainProvider implements ContextProvider {

        private final ImmutableSet<String> providedContexts = ImmutableSet
                .of(ConfigurationContext.APPDOMAIN.name());

        @Override
        public String getContextParam(final String name) {
            return "1";
        }

        @Override
        public Set<String> getProvidedContexts() {
            return providedContexts;
        }
    }

}
