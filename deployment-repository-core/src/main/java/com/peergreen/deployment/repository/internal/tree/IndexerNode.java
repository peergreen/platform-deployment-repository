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

import com.peergreen.deployment.repository.BaseNode;
import com.peergreen.deployment.repository.Node;
import com.peergreen.deployment.repository.node.SimpleNode;

import java.net.URI;

/**
 * @author Mohammed Boukada
 */
public class IndexerNode<T> extends SimpleNode<T> implements Node<T> {

    public IndexerNode(T data) {
        super(data);
    }

    public IndexerNode<T> getNode(String nodeName) {
        for (Node<T> node : getChildren()) {
            BaseNode data = (BaseNode) node.getData();
            if (nodeName.equals(data.getName())) return (IndexerNode<T>) node;
        }
        return null;
    }

    public IndexerNode<T> getNode(URI uri) {
        if (((BaseNode) getData()).getUri().equals(uri)) {
            return this;
        }
        for (Node<T> node : getChildren()) {
            IndexerNode<T> iNode = (IndexerNode<T>) node;
            BaseNode data = (BaseNode) iNode.getData();
            if (uri.toString().startsWith(data.getUri().toString())) {
                if (uri.equals(data.getUri())) {
                    return iNode;
                } else {
                    return iNode.getNode(uri);
                }
            }
        }
        return null;
    }

    public void addChild(Node<T> node) {
        if (!containsNode(node)) {
            getChildren().add(node);
        }
    }

    public boolean containsNode(Node<T> searchedNode) {
        BaseNode searchedData = (BaseNode) searchedNode.getData();
        for (Node<T> node : getChildren()) {
            BaseNode data = (BaseNode) node.getData();
            if (data.getUri().equals(searchedData.getUri())) {
                return true;
            }
        }
        return false;
    }

    public void merge(Node<T> nodeToMerge) {
        for (Node<T> node : nodeToMerge.getChildren()) {
            if (containsNode(node)) {
                BaseNode data = (BaseNode) node.getData();
                getNode(data.getUri()).merge(node);
            } else {
                addChild(nodeToMerge);
            }
        }
    }
}
