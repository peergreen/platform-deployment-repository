/*
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.deployment.repository.internal.directory;

import com.peergreen.deployment.repository.Attributes;
import com.peergreen.deployment.repository.DirectoryRepositoryService;
import com.peergreen.deployment.repository.internal.base.AttributesName;
import com.peergreen.deployment.repository.BaseNode;
import com.peergreen.deployment.repository.internal.base.InternalAttributes;
import com.peergreen.deployment.repository.internal.exception.NoDirectoryException;
import com.peergreen.deployment.repository.internal.tree.IndexerNode;
import com.peergreen.deployment.repository.internal.tree.IndexerGraph;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mohammed Boukada
 */
@Component(name = "com.peergreen.deployment.repository.directory")
@Provides(properties = @StaticServiceProperty(name = "repository.type", type = "java.lang.String", mandatory = true))
public class DirectoryRepositoryServiceImpl implements DirectoryRepositoryService {

    @Property(name = "repository.name")
    private String name;
    @Property(name = "repository.url", mandatory = true)
    private String url;

    public IndexerGraph<BaseNode> list(String filter) {
        IndexerGraph<BaseNode> graph = new IndexerGraph<>();
        try {
            File dir = checkDirectory();

            // Adds root node
            BaseNode root = new BaseNode(name, dir.toURI(), !dir.isDirectory());
            IndexerNode<BaseNode> rootNode = new IndexerNode<BaseNode>(root);
            graph.addNode(rootNode);

            for (File file : dir.listFiles()) {
                addFileToGraph(file, rootNode, filter);
            }
        } catch (URISyntaxException | FileNotFoundException | NoDirectoryException e) {
            // do nothing
        }
        return graph;
    }

    @Override
    public Attributes getAttributes() {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(AttributesName.NAME, name);
        attributes.put(AttributesName.URL, url);
        return new InternalAttributes(attributes);
    }

    private File checkDirectory() throws FileNotFoundException, NoDirectoryException, URISyntaxException {
        File file = new File(url);
        if (!file.exists()) {
            try {
                file = new File(new URI(url));
            } catch (IllegalArgumentException e) {
                if (!file.exists()) throw new FileNotFoundException("'" + url + "' not found.");
            }
        }
        if (!file.isDirectory()) {
            throw new NoDirectoryException("'" + url + "' is not a directory");
        }
        return file;
    }

    private void addFileToGraph(File file, IndexerNode<BaseNode> node, String filter) {
        BaseNode fileNodeData = new BaseNode(file.getName(), file.toURI(), !file.isDirectory());
        IndexerNode<BaseNode> fileNode = new IndexerNode<BaseNode>(fileNodeData);
        if (filter != null && !"".equals(filter)) {
            if (file.getName().matches(filter)) node.addChild(fileNode);
        } else {
            node.addChild(fileNode);
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                addFileToGraph(f, fileNode, filter);
            }
            if (filter != null && !"".equals(filter)
                    && !file.getName().matches(filter) && fileNode.getChildren().size() > 0)
                node.addChild(fileNode);
        }
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void setUrl(String url) {
        this.url = url;
    }
}
