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

package org.zalando.baigan.etcd.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.baigan.etcd.model.KeyResultNode;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

/**
 * @author mchand
 */
public final class EtcdClient {

    private final Logger LOG = LoggerFactory.getLogger(EtcdClient.class);

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new GuavaModule());

    private final String etcdUrl;

    public EtcdClient(final String url) {
        etcdUrl = url;
    }

    public final Optional<String> get(@Nonnull final String key) {
        try {
            final URL url = new URL(etcdUrl + key);

            final KeyResultNode resultNode = mapper.readValue(url,
                    KeyResultNode.class);
            final String response = resultNode.getNode().getValue();

            return Optional.ofNullable(response);
        } catch (IOException e) {
            LOG.warn("There was an exception trying to get key: " + key, e);
        } catch (NullPointerException npe) {
            LOG.warn("There was an exception trying to get key: " + key);
        }
        return Optional.empty();
    }
}
