/*
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.deployment.repository.internal.tree;

import com.peergreen.deployment.repository.BaseNode;
import com.peergreen.deployment.repository.Graph;
import com.peergreen.deployment.repository.Node;
import com.peergreen.deployment.repository.graph.SimpleGraph;

import java.util.Collection;

/**
 * @author Mohammed Boukada
 */
public class IndexerGraph<T> extends SimpleGraph<T> implements Graph<T> {

    public IndexerGraph() {
        super();
    }

    public IndexerGraph(Collection<Node<T>> initialNodes) {
        super(initialNodes);
    }

    public IndexerNode<T> getNode(String nodeName) {
        for (Node<T> node : getNodes()) {
            BaseNode data = (BaseNode) node.getData();
            if (nodeName.equals(data.getName())) return (IndexerNode<T>) node;
        }
        return null;
    }

    public void addNode(Node<T> node) {
        getNodes().add(node);
    }
}
