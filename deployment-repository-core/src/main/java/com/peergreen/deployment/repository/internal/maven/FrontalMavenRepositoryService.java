/*
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.deployment.repository.internal.maven;

import com.peergreen.deployment.repository.Attributes;
import com.peergreen.deployment.repository.BaseNode;
import com.peergreen.deployment.repository.maven.MavenNode;
import com.peergreen.deployment.repository.MavenRepositoryService;
import com.peergreen.deployment.repository.Node;
import com.peergreen.deployment.repository.RepositoryType;
import com.peergreen.deployment.repository.internal.base.AttributesName;
import com.peergreen.deployment.repository.internal.base.InternalAttributes;
import com.peergreen.deployment.repository.internal.tree.IndexerGraph;
import com.peergreen.deployment.repository.search.Query;
import com.peergreen.deployment.repository.search.RepositoryQuery;
import com.peergreen.deployment.repository.view.Repository;
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mohammed Boukada
 */
@Component
@Instantiate
@Provides(properties = @StaticServiceProperty(name = "type", type = "java.lang.String", value = RepositoryType.FACADE, mandatory = true))
public class FrontalMavenRepositoryService implements MavenRepositoryService {

    private Map<String, MavenRepositoryService> mavenRepositoryServices = new ConcurrentHashMap<String, MavenRepositoryService>();

    public IndexerGraph<BaseNode> list(String filter) {
        Set<Node<BaseNode>> nodes = new HashSet<Node<BaseNode>>();
        for (Map.Entry<String, MavenRepositoryService> mavenRepositoryService : mavenRepositoryServices.entrySet()) {
            nodes.addAll(mavenRepositoryService.getValue().list(filter).getNodes());
        }
        return new IndexerGraph<>(nodes);
    }

    public IndexerGraph<MavenNode> list(Query... queries) {
        Set<Node<MavenNode>> nodes = new HashSet<Node<MavenNode>>();
        String[] repositories = getRepositoryUrls(queries);
        if (getRepositoryUrls(queries).length == 0) {
            for (Map.Entry<String, MavenRepositoryService> mavenRepositoryService : mavenRepositoryServices.entrySet()) {
                nodes.addAll(mavenRepositoryService.getValue().list(queries).getNodes());
            }
        } else {
            for (String repositoryUrl : repositories) {
                nodes.addAll(mavenRepositoryServices.get(repositoryUrl).list(queries).getNodes());
            }
        }
        return new IndexerGraph<>(nodes);
    }

    @Override
    public Attributes getAttributes() {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(AttributesName.URLS, getUrls());
        return new InternalAttributes(attributes);
    }

    private String[] getRepositoryUrls(Query[] queries) {
        String[] repositories = new String[0];
        for (Query query : queries) {
            if (query instanceof RepositoryQuery) {
                String[] queryRepositories = ((RepositoryQuery) query).getRepositories();
                String[] tmp = new String[repositories.length + queryRepositories.length];
                System.arraycopy(queryRepositories, 0, tmp, repositories.length, queryRepositories.length);
                repositories = tmp;
            }
        }
        return repositories;
    }

    private Object getUrls() {
        Collection<String> urls = new ArrayList<String>();
        for (Map.Entry<String, MavenRepositoryService> mavenRepositoryService : mavenRepositoryServices.entrySet()) {
            urls.add(mavenRepositoryService.getKey());
        }
        return urls;
    }

    @Bind(optional = true, aggregate = true, filter = "(!(type=" + RepositoryType.FACADE + "))")
    public void bindMavenRepositoryService(MavenRepositoryService mavenRepositoryService) {
        mavenRepositoryServices.put(mavenRepositoryService.getAttributes().as(Repository.class).getUrl(),
                mavenRepositoryService);
    }

    @Unbind
    public void unbindMavenRepositoryService(MavenRepositoryService mavenRepositoryService) {
        mavenRepositoryServices.remove(mavenRepositoryService.getAttributes().as(Repository.class).getUrl());
    }
}
