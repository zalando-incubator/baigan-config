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

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;

/**
 * Default implementation for {@link ContextProviderRegistry} and
 * {@link ContextProviderRetriever} interfaces.
 *
 * @author mchand
 *
 */
@Component
public class ConfigurationContextProviderRegistryImpl
        implements ContextProviderRegistry, ContextProviderRetriever {

    private final Multimap<String, ContextProvider> providerMap = HashMultimap
            .create(10, 5);

    @Override
    public void register(@Nonnull final ContextProvider contextProvider) {
        Preconditions.checkNotNull(contextProvider,
                "Attempt to register null as a context provider!");
        contextProvider.getProvidedContexts().forEach(context -> providerMap.put(context, contextProvider));
    }

    @Override
    @Nonnull
    public Collection<ContextProvider> getProvidersFor(
            @Nonnull final String contextName) {
        Preconditions.checkArgument(StringUtils.hasLength(contextName),
                "Attempt to access value for an empty context name!");
        return ImmutableSet.copyOf(providerMap.get(contextName));
    }

    @Override
    public Set<String> getContextParameterKeys() {
        return ImmutableSet.copyOf(providerMap.keySet());
    }

}
