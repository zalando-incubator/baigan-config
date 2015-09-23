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

package org.zalando.baigan.etcd.model;

import java.util.List;

/**
 * @author mchand
 */
public class DirNode extends AbstractNode {
    private boolean dir;
    private List<KeyNode> nodes;

    public boolean isDir() {
        return dir;
    }

    public void setDir(final boolean dir) {
        this.dir = dir;
    }

    public List<KeyNode> getNodes() {
        return nodes;
    }

    public void setNodes(final List<KeyNode> nodes) {
        this.nodes = nodes;
    }
}
