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
import com.peergreen.deployment.repository.BaseNode;
import com.peergreen.deployment.repository.DirectoryRepositoryService;
import com.peergreen.deployment.repository.Node;
import com.peergreen.deployment.repository.RepositoryType;
import com.peergreen.deployment.repository.internal.base.AttributesName;
import com.peergreen.deployment.repository.internal.base.InternalAttributes;
import com.peergreen.deployment.repository.internal.tree.IndexerGraph;
import com.peergreen.deployment.repository.view.Repository;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mohammed Boukada
 */
@Component
@Instantiate
@Provides(properties = @StaticServiceProperty(name = "repository.type", type = "java.lang.String", value = RepositoryType.FACADE, mandatory = true))
public class FrontalDirectoryRepositoryService implements DirectoryRepositoryService {

    private Map<String, DirectoryRepositoryService> directoryRepositoryServices = new ConcurrentHashMap<String, DirectoryRepositoryService>();

    public IndexerGraph<BaseNode> list(String filter) {
        Set<Node<BaseNode>> nodes = new HashSet<Node<BaseNode>>();
        for (Map.Entry<String, DirectoryRepositoryService> directoryRepositoryService : directoryRepositoryServices.entrySet()) {
            nodes.addAll(directoryRepositoryService.getValue().list(filter).getNodes());
        }
        return new IndexerGraph<BaseNode>(nodes);
    }

    @Override
    public Attributes getAttributes() {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(AttributesName.URLS, getUrls());
        return new InternalAttributes(attributes);
    }

    private Collection<String> getUrls() {
        Collection<String> urls = new ArrayList<String>();
        for (Map.Entry<String, DirectoryRepositoryService> directoryRepositoryService : directoryRepositoryServices.entrySet()) {
            urls.add(directoryRepositoryService.getKey());
        }
        return urls;
    }

    @Bind(optional = true, aggregate = true, filter = "(!(repository.type=" + RepositoryType.FACADE + "))")
    public void bindDirectoryRepositoryService(DirectoryRepositoryService directoryRepositoryService) {
        directoryRepositoryServices.put(directoryRepositoryService.getAttributes().as(Repository.class).getUrl(),
                directoryRepositoryService);
    }

    @Unbind
    public void unbindDirectoryRepositoryService(DirectoryRepositoryService directoryRepositoryService) {
        directoryRepositoryServices.remove(directoryRepositoryService.getAttributes().as(Repository.class).getUrl());
    }

    @Override
    public List<Node<BaseNode>> getChildren(URI uri) {
        if (uri == null) {
            List<Node<BaseNode>> nodes = new ArrayList<>();
            for (Map.Entry<String, DirectoryRepositoryService> directoryRepositoryService : directoryRepositoryServices.entrySet()) {
                DirectoryRepositoryServiceImpl impl = (DirectoryRepositoryServiceImpl) directoryRepositoryService.getValue();
                nodes.addAll(impl.getCache().getNodes());
            }
            return nodes;
        } else {
            for (Map.Entry<String, DirectoryRepositoryService> directoryRepositoryService : directoryRepositoryServices.entrySet()) {
                if (uri.toString().startsWith(directoryRepositoryService.getKey())) {
                    return directoryRepositoryService.getValue().getChildren(uri);
                }
            }
        }
        return null;
    }
}
