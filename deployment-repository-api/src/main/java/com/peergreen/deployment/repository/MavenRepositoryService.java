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

import com.peergreen.deployment.repository.maven.MavenArtifactInfo;
import com.peergreen.deployment.repository.maven.MavenNode;
import com.peergreen.deployment.repository.search.Query;

import java.net.URI;
import java.util.List;

/**
 * @author Mohammed Boukada
 */
public interface MavenRepositoryService extends RepositoryService {
    Graph<MavenNode> list(Query... queries);
    List<Node<MavenNode>> getChildren(URI uri, MavenArtifactInfo.Type parentType);
    List<Node<MavenNode>> getChildren(URI uri, MavenArtifactInfo.Type parentType, boolean refresh);
}
