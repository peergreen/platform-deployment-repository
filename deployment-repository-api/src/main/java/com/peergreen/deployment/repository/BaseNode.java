/*
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.deployment.repository;

import java.net.URI;

/**
 * @author Mohammed Boukada
 */
public class BaseNode {
    private String name;
    private URI uri;
    private Long lastModified;
    private boolean isLeaf;

    public BaseNode(String name, URI uri, boolean isLeaf) {
        this.name = name;
        this.uri = uri;
        this.isLeaf = isLeaf;
    }

    public String getName() {
        return name;
    }

    public URI getUri() {
        return uri;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isLeaf() {
        return isLeaf;
    }
}
