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

package org.zalando.baigan.proxy.handler;

import java.util.Set;

import com.google.common.reflect.AbstractInvocationHandler;

/**
 * This class provides an abstraction on the Method invocation handler.
 *
 * @author mchand
 *
 */
public abstract class ContextAwareMethodInvocationHandler
        extends AbstractInvocationHandler {

    private Set<String> contextParameterKeys;

    public void setContextParameterKeys(
            final Set<String> contextParameterKeys) {
        this.contextParameterKeys = contextParameterKeys;
    }

    public Set<String> getContextParameterKeys() {
        return contextParameterKeys;
    }

}
