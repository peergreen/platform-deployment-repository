/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.deployment.repository.internal.tree;

import java.net.URI;
import java.util.Collection;

import com.peergreen.deployment.repository.BaseNode;
import com.peergreen.deployment.repository.Graph;
import com.peergreen.deployment.repository.Node;
import com.peergreen.deployment.repository.graph.SimpleGraph;

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
            if (nodeName.equals(data.getName())) {
                return (IndexerNode<T>) node;
            }
        }
        return null;
    }

    public IndexerNode<T> getNode(URI uri) {
        for (Node<T> node : getNodes()) {
            IndexerNode<T> iNode = (IndexerNode<T>) node;
            BaseNode data = (BaseNode) iNode.getData();
            if (uri != null && uri.toString().startsWith(data.getUri().toString())) {
                if (uri.equals(data.getUri())) {
                    return iNode;
                } else {
                    return iNode.getNode(uri);
                }
            }
        }
        return null;
    }

    public void addNode(Node<T> node) {
        getNodes().add(node);
    }

    public boolean containsNode(Node<T> searchedNode) {
        BaseNode searchedData = (BaseNode) searchedNode.getData();
        for (Node<T> node : getNodes()) {
            BaseNode data = (BaseNode) node.getData();
            if (data.getUri().equals(searchedData.getUri())) {
                return true;
            }
        }
        return false;
    }

    public void merge(IndexerGraph<T> graph) {
        for (Node<T> node : graph.getNodes()) {
            if (containsNode(node)) {
                BaseNode data = (BaseNode) node.getData();
                getNode(data.getUri()).merge(node);
            } else {
                addNode(node);
            }
        }
    }
}
