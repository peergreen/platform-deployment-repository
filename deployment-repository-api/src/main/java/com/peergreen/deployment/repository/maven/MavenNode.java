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
import org.apache.maven.index.ArtifactInfo;

import java.net.URI;

/**
 * @author Mohammed Boukada
 */
public class MavenNode extends BaseNode {

    private ArtifactInfo artifactInfo;
    private String type;

    public MavenNode(String name, URI uri, String type, ArtifactInfo artifactInfo) {
        super(name, uri);
        this.type = type;
        this.artifactInfo = artifactInfo;
    }

    public String getType() {
        return type;
    }

    public ArtifactInfo getArtifactInfo() {
        return artifactInfo;
    }
}
