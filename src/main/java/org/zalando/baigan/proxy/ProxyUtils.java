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

package org.zalando.baigan.proxy;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;

import java.lang.reflect.Method;

/**
 * The class to contain utility methods used in proxying configuration beans.
 *
 * @author mchand
 *
 */
public class ProxyUtils {
    private static final String NAMESPACE_SEPARATOR = ".";

    public static String createKey(final Class<?> clazz, Method method) {
        final String methodName = method.getName();
        final String nameSpace = clazz.getSimpleName();

        return ProxyUtils.dottify(nameSpace) + "."
                + ProxyUtils.dottify(methodName);
    }

    private static String dottify(final String text) {

        if (Strings.isNullOrEmpty(text)) {
            return NAMESPACE_SEPARATOR;
        }

        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, text)
                .replace("_", NAMESPACE_SEPARATOR);

    }
}
