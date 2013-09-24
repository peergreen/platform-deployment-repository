/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.deployment.repository.internal.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import com.peergreen.deployment.repository.Attributes;
import com.peergreen.deployment.repository.BaseNode;
import com.peergreen.deployment.repository.Graph;
import com.peergreen.deployment.repository.Node;
import com.peergreen.deployment.repository.RepositoryManager;
import com.peergreen.deployment.repository.RepositoryService;
import com.peergreen.deployment.repository.RepositoryType;
import com.peergreen.deployment.repository.internal.maven.MavenRepositoryServiceImpl;
import com.peergreen.deployment.repository.internal.tree.IndexerGraph;
import com.peergreen.deployment.repository.view.Facade;
import com.peergreen.deployment.repository.view.Repository;

/**
 * @author Mohammed Boukada
 */
@Component
@Instantiate
@Provides(properties = @StaticServiceProperty(name = "repository.type", type = "java.lang.String", value = RepositoryType.SUPER, mandatory = true))
public class FrontalBaseRepositoryService implements RepositoryService, RepositoryManager {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(FrontalBaseRepositoryService.class);

    @Requires(from = "com.peergreen.deployment.repository.directory")
    private Factory directoryRepositoryFactory;
    @Requires(from = "com.peergreen.deployment.repository.maven")
    private Factory mavenRepositoryFactory;

    private Map<String, ComponentInstance> repositoryInstances = new ConcurrentHashMap<>();
    private List<Repository> repositories = new CopyOnWriteArrayList<>();
    private List<RepositoryService> facadeRepositoryServices = new CopyOnWriteArrayList<RepositoryService>();
    private DefaultPlexusContainer plexusContainer;

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

    @Bind(optional = true, aggregate = true, filter = "(!(|(" + AttributesName.TYPE + "=" + RepositoryType.SUPER + ")" +
                                                          "(" + AttributesName.TYPE + "=" + RepositoryType.FACADE + ")))")
    public void bindRepository(RepositoryService repositoryService) {
        repositories.add(repositoryService.getAttributes().as(Repository.class));
    }

    @Unbind
    public void unbindRepository(RepositoryService repositoryService) {
        repositories.remove(repositoryService.getAttributes().as(Repository.class));
    }

    @Override
    public boolean addRepository(String url, String name, String type) {
        if (!containsRepository(url)) {
            Factory factory = null;
            Dictionary<String, Object> properties = new Hashtable<>();
            switch (type) {
                case RepositoryType.DIRECTORY:
                    factory = directoryRepositoryFactory;
                    break;
                case RepositoryType.MAVEN:
                    factory = mavenRepositoryFactory;
                    break;
            }

            if (factory != null && url != null) {
                properties.put("repository.type", type);
                properties.put("repository.name", (name == null) ? url : name);
                properties.put("repository.url", url);
                try {
                    repositoryInstances.put(url, factory.createComponentInstance(properties));
                } catch (UnacceptableConfiguration | MissingHandlerException | ConfigurationException e) {
                    LOGGER.error("Fail to add repository ''{0}''", url, e);
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean removeRepository(String url) {
        if (repositoryInstances.containsKey(url)) {
            repositoryInstances.get(url).stop();
            repositoryInstances.get(url).dispose();
            repositoryInstances.remove(url);
            return true;
        }
        return false;
    }

    @Override
    public List<Repository> getRepositories() {
        return repositories;
    }

    @Override
    public void loadRepositoriesInCache() {
        File repositoriesDirectory = new File(MavenRepositoryServiceImpl.REPOSITORIES_FOLDER);
        if (repositoriesDirectory.exists() && repositoriesDirectory.isDirectory()) {
            for (File repositoryDir : repositoriesDirectory.listFiles()) {
                if (repositoryDir.isDirectory()) {
                    Properties props = new Properties();
                    File repositoryProperties = new File(repositoryDir.toString() + "/index/repository.properties");
                    try {
                        props.load(new FileInputStream(repositoryProperties));
                        addRepository(props.getProperty("repository.url"),
                                      props.getProperty("repository.name"),
                                      props.getProperty("repository.type"));
                    } catch (IOException e) {
                        LOGGER.error("Cannot read repository properties for ''{0}''", repositoryDir.getName(), e);
                    }
                }
            }
        }
    }

    @Invalidate
    public void stop() {
        for (Map.Entry<String, ComponentInstance> instance : repositoryInstances.entrySet()) {
            removeRepository(instance.getKey());
        }
    }

    private boolean containsRepository(String url) {
        if (repositoryInstances.containsKey(url)) {
            return ComponentInstance.VALID == repositoryInstances.get(url).getState() || removeRepository(url);
        } else {
            if (url.charAt(url.length() - 1) == '/') {
                String s = url.substring(0, url.length() - 1);
                return repositoryInstances.containsKey(s);
            }
            return false;
        }
    }
}
