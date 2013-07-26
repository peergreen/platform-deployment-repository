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
import com.peergreen.deployment.repository.maven.MavenNode;
import com.peergreen.deployment.repository.internal.tree.IndexerGraph;
import com.peergreen.deployment.repository.view.Facade;
import com.peergreen.deployment.repository.view.Repository;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * @author Mohammed Boukada
 */
public class TestFrontalMavenRepositoryService {
    private final static String PEERGREEN_PUBLIC_REPOSITORY = "https://forge.peergreen.com/repository/content/repositories/releases";
    private final static String OW2_PUBLIC_REPOSITORY = "http://repository.ow2.org/nexus/content/repositories/releases";

    private FrontalMavenRepositoryService frontalMavenRepositoryService;
    private MavenRepositoryServiceImpl mavenRepositoryService1;
    private MavenRepositoryServiceImpl mavenRepositoryService2;

    @BeforeClass
    public void configure() throws ComponentLookupException, PlexusContainerException, IOException {
        frontalMavenRepositoryService = new FrontalMavenRepositoryService();

        mavenRepositoryService1 = new MavenRepositoryServiceImpl();
        mavenRepositoryService1.setUrl(PEERGREEN_PUBLIC_REPOSITORY);
        mavenRepositoryService1.setName("Peergreen Public Repository");
        mavenRepositoryService1.init();

        mavenRepositoryService2 = new MavenRepositoryServiceImpl();
        mavenRepositoryService2.setUrl(OW2_PUBLIC_REPOSITORY);
        mavenRepositoryService2.setName("OW2 Public Repository");
        mavenRepositoryService2.init();

        frontalMavenRepositoryService.bindMavenRepositoryService(mavenRepositoryService1);
        frontalMavenRepositoryService.bindMavenRepositoryService(mavenRepositoryService2);
    }

    @AfterClass
    public void stop() throws IOException {
        mavenRepositoryService1.closeIndexingContext();
        mavenRepositoryService2.closeIndexingContext();
    }

    @Test
    public void testGetAttributes() {
        Attributes attributes = frontalMavenRepositoryService.getAttributes();
        Repository repository = attributes.as(Repository.class);
        Assert.assertEquals(repository.getName(), null);
        Assert.assertEquals(repository.getUrl(), null);
        Facade facade = attributes.as(Facade.class);
        Assert.assertNotNull(facade);
        Assert.assertTrue(facade.getUrls().contains(PEERGREEN_PUBLIC_REPOSITORY));
        Assert.assertTrue(facade.getUrls().contains(OW2_PUBLIC_REPOSITORY));
    }

    @Test
    public void testFetchArtifactsFromTwoRepositories() {
        IndexerGraph<MavenNode> graph = frontalMavenRepositoryService.list();
        Assert.assertEquals(graph.getNodes().size(), 2);
        Assert.assertNotNull(graph.getNode("Peergreen Public Repository"));
        Assert.assertNotNull(graph.getNode("OW2 Public Repository"));
    }
}
