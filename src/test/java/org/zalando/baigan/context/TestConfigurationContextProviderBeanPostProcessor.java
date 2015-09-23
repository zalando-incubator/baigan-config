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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.zalando.baigan.context.ConfigurationContextProviderBeanPostprocessor;
import org.zalando.baigan.context.ContextProviderRegistry;
import org.zalando.baigan.provider.ContextProvider;

/**
 *
 * @author mchand
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestConfigurationContextProviderBeanPostProcessor {

    @Mock
    private ContextProviderRegistry registry;

    @InjectMocks
    private ConfigurationContextProviderBeanPostprocessor processor;

    @Mock
    private ContextProvider provider;

    @Test
    public void testMessenger() {

        processor.postProcessAfterInitialization(provider, "contextProvider");
        ArgumentCaptor<ContextProvider> contextProviderCaptor = ArgumentCaptor
                .forClass(ContextProvider.class);

        verify(registry, times(1))
                .register(org.mockito.Matchers.any(ContextProvider.class));

        verify(registry).register(contextProviderCaptor.capture());
        assertThat(contextProviderCaptor.getValue(), equalTo(provider));
    }
}
