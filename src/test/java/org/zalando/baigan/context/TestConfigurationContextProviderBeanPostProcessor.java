/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zalando.baigan.context;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.baigan.provider.ContextProvider;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author mchand
 */
@ExtendWith(MockitoExtension.class)
public class TestConfigurationContextProviderBeanPostProcessor {

    private final ContextProvider provider = mock(ContextProvider.class);

    private final ContextProviderRegistry registry = mock(ContextProviderRegistry.class);

    private ConfigurationContextProviderBeanPostprocessor processor = new ConfigurationContextProviderBeanPostprocessor(() -> registry);

    @Test
    public void testMessenger() {

        processor.postProcessAfterInitialization(provider, "contextProvider");
        ArgumentCaptor<ContextProvider> contextProviderCaptor = ArgumentCaptor
                .forClass(ContextProvider.class);

        verify(registry, times(1))
                .register(any(ContextProvider.class));

        verify(registry).register(contextProviderCaptor.capture());
        assertThat(contextProviderCaptor.getValue(), equalTo(provider));
    }
}
