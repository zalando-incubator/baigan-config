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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.baigan.context.ConfigurationContext;
import org.zalando.baigan.context.ConfigurationContextProviderBeanPostprocessor;
import org.zalando.baigan.context.ConfigurationContextProviderRegistryImpl;
import org.zalando.baigan.context.ContextProviderRetriever;
import org.zalando.baigan.context.ConfigurationContextProvisionIT.StaticAppdomainProvider;
import org.zalando.baigan.provider.ContextProvider;

import com.google.common.collect.ImmutableSet;

/**
 *
 * @author Mohammed Amjed
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
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
        assertThat(contextValue, Matchers.equalTo("1"));
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
