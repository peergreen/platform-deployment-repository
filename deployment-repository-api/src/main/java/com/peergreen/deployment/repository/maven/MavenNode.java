/*
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.deployment.repository.maven;

import com.peergreen.deployment.repository.BaseNode;

import java.net.URI;

/**
 * @author Mohammed Boukada
 */
public class MavenNode extends BaseNode {

    private MavenArtifactInfo artifactInfo;
    private String type;

    public MavenNode(String name, URI uri, boolean isLeaf, String type, MavenArtifactInfo artifactInfo) {
        super(name, uri, isLeaf);
        this.type = type;
        this.artifactInfo = artifactInfo;
    }

    public String getType() {
        return type;
    }

    public void setArtifactInfo(MavenArtifactInfo artifactInfo) {
        this.artifactInfo = artifactInfo;
    }

    public MavenArtifactInfo getArtifactInfo() {
        return artifactInfo;
    }
}
