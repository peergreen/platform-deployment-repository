/*
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.deployment.repository.internal.base;

import com.peergreen.deployment.repository.Attributes;
import com.peergreen.deployment.repository.BaseNode;
import com.peergreen.deployment.repository.Graph;
import com.peergreen.deployment.repository.Node;
import com.peergreen.deployment.repository.RepositoryService;
import com.peergreen.deployment.repository.RepositoryType;
import com.peergreen.deployment.repository.internal.tree.IndexerGraph;
import com.peergreen.deployment.repository.view.Facade;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Mohammed Boukada
 */
@Component
@Instantiate
@Provides(properties = @StaticServiceProperty(name = "repository.type", type = "java.lang.String", value = RepositoryType.SUPER, mandatory = true))
public class FrontalBaseRepositoryService implements RepositoryService {

    private List<RepositoryService> facadeRepositoryServices = new CopyOnWriteArrayList<RepositoryService>();

    protected IndexerGraph<BaseNode> listFiles(String filter) {
        Set<Node<BaseNode>> nodes = new HashSet<Node<BaseNode>>();
        for (RepositoryService repositoryService : facadeRepositoryServices) {
            nodes.addAll(repositoryService.list(filter).getNodes());
        }
        return new IndexerGraph<>(nodes);
    }

    @Override
    public Graph<BaseNode> list(String filter) {
        return listFiles(filter);
    }

    @Override
    public Attributes getAttributes() {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(AttributesName.URLS, getUrls());
        return new InternalAttributes(attributes);
    }

    private Collection<String> getUrls() {
        Collection<String> urls = new ArrayList<String>();
        for (RepositoryService facadeRepositoryService : facadeRepositoryServices) {
            urls.addAll(facadeRepositoryService.getAttributes().as(Facade.class).getUrls());
        }
        return urls;
    }

    @Bind(optional = true, aggregate = true, filter = "(repository.type=" + RepositoryType.FACADE + ")")
    public void bindFacadeRepositoryService(RepositoryService repositoryService) {
        facadeRepositoryServices.add(repositoryService);
    }

    @Unbind
    public void unbindFacadeRepositoryService(RepositoryService repositoryService) {
        facadeRepositoryServices.remove(repositoryService);
    }
}
