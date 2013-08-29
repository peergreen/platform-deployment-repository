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
import com.peergreen.deployment.repository.internal.tree.IndexerNode;
import com.peergreen.deployment.repository.search.Queries;
import com.peergreen.deployment.repository.search.Query;
import com.peergreen.deployment.repository.view.Repository;
import org.apache.maven.index.ArtifactInfo;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.peergreen.deployment.repository.search.Queries.*;

/**
 * @author Mohammed Boukada
 */
public class TestMavenRepositoryService {

    private final static String PEERGREEN_PUBLIC_REPOSITORY = "https://forge.peergreen.com/repository/content/repositories/releases";
    private MavenRepositoryServiceImpl mavenRepositoryService;

    @BeforeClass
    public void configure() throws ComponentLookupException, PlexusContainerException, IOException {
        mavenRepositoryService = new MavenRepositoryServiceImpl();
        mavenRepositoryService.setUrl(PEERGREEN_PUBLIC_REPOSITORY);
        mavenRepositoryService.setName("Peergreen Public Repository");
        mavenRepositoryService.init();
    }

    @AfterClass
    public void stop() throws IOException {
        mavenRepositoryService.closeIndexingContext();
    }

    @Test
    public void test() {
        Query query = Queries.groupId(and(or(eq("g1"), eq("g2")), and(eq("g3"), eq("g4"))));
        org.apache.lucene.search.Query luceneQuery = mavenRepositoryService.createQuery(new Query[]{query});
    }

    @Test
    public void testGetAttributes() {
        Attributes attributes = mavenRepositoryService.getAttributes();
        Repository repositoryInfo = attributes.as(Repository.class);
        Assert.assertNotNull(repositoryInfo);
        Assert.assertEquals(repositoryInfo.getName(), "Peergreen Public Repository");
        Assert.assertEquals(repositoryInfo.getUrl(), PEERGREEN_PUBLIC_REPOSITORY);
    }

    @Test
    public void testFetchAllMavenArtifacts() throws URISyntaxException {
        IndexerGraph<MavenNode> graph = mavenRepositoryService.list();
        Assert.assertTrue(graph.getNodes().size() >= 1);
        IndexerNode<MavenNode> rootNode = (IndexerNode<MavenNode>) graph.getNodes().iterator().next();
        URI rootURI = rootNode.getData().getUri();
        Assert.assertNotNull(rootNode);
        Assert.assertEquals(rootNode.getData().getUri(), new URI(PEERGREEN_PUBLIC_REPOSITORY));
        Assert.assertEquals(rootNode.getData().getArtifactInfo().type, REPOSITORY);
        Assert.assertEquals(rootNode.getData().getName(), "Peergreen Public Repository");

        IndexerNode<MavenNode> comNode = rootNode.getNode("com");
        Assert.assertNotNull(comNode);
        Assert.assertEquals(comNode.getData().getArtifactInfo().type, GROUP_ID);
        URI expectedComNodeURI = new URI(rootURI.toString() + "/com");
        Assert.assertEquals(comNode.getData().getUri(), expectedComNodeURI);

        IndexerNode<MavenNode> peergreenNode = comNode.getNode("peergreen");
        Assert.assertNotNull(peergreenNode);
        Assert.assertTrue(peergreenNode.getChildren().size() >= 2);
        Assert.assertEquals(peergreenNode.getData().getArtifactInfo().type, GROUP_ID);
        URI expectedPeergreenNodeURI = new URI(expectedComNodeURI.toString() + "/peergreen");
        Assert.assertEquals(peergreenNode.getData().getUri(), expectedPeergreenNodeURI);

        IndexerNode<MavenNode> communityNode = peergreenNode.getNode("community");
        Assert.assertNotNull(communityNode);
        Assert.assertTrue(communityNode.getChildren().size() >= 7);
        Assert.assertEquals(communityNode.getData().getArtifactInfo().type, GROUP_ID);
        URI expectedCommunityNodeURI = new URI(expectedPeergreenNodeURI.toString() + "/community");
        Assert.assertEquals(communityNode.getData().getUri(), expectedCommunityNodeURI);

        IndexerNode<MavenNode> exampleNode = peergreenNode.getNode("example");
        Assert.assertNotNull(exampleNode);
        Assert.assertEquals(exampleNode.getData().getArtifactInfo().type, GROUP_ID);
        URI expectedExampleNodeURI = new URI(expectedPeergreenNodeURI.toString() + "/example");
        Assert.assertEquals(exampleNode.getData().getUri(), expectedExampleNodeURI);

        IndexerNode<MavenNode> paxexamNode = exampleNode.getNode("paxexam");
        Assert.assertNotNull(paxexamNode);
        Assert.assertTrue(paxexamNode.getChildren().size() >= 1);
        Assert.assertEquals(paxexamNode.getData().getArtifactInfo().type, GROUP_ID);
        URI expectedPaxexamNodeURI = new URI(expectedExampleNodeURI.toString() + "/paxexam");
        Assert.assertEquals(paxexamNode.getData().getUri(), expectedPaxexamNodeURI);

        IndexerNode<MavenNode> helloServiceNode = paxexamNode.getNode("paxexam-hello-service");
        Assert.assertNotNull(helloServiceNode);
        Assert.assertEquals(helloServiceNode.getData().getArtifactInfo().type, ARTIFACT_ID);
        URI expectedHelloServiceNodeURI = new URI(expectedPaxexamNodeURI.toString() + "/paxexam-hello-service");
        Assert.assertEquals(helloServiceNode.getData().getUri(), expectedHelloServiceNodeURI);

        IndexerNode<MavenNode> helloServiceVersion100Node = helloServiceNode.getNode("1.0.0");
        Assert.assertNotNull(helloServiceVersion100Node);
        Assert.assertEquals(helloServiceVersion100Node.getData().getArtifactInfo().type, VERSION);
        URI expectedHelloServiceVersion100NodeURI = new URI(expectedHelloServiceNodeURI.toString() + "/1.0.0");
        Assert.assertEquals(helloServiceVersion100Node.getData().getUri(), expectedHelloServiceVersion100NodeURI);

        IndexerNode<MavenNode> helloServiceJarFileNode = helloServiceVersion100Node.getNode("paxexam-hello-service-1.0.0.jar");
        Assert.assertNotNull(helloServiceJarFileNode);
        Assert.assertEquals(helloServiceJarFileNode.getData().getArtifactInfo().type, ARCHIVE);
        URI expectedHelloServiceJarFileNodeURI = new URI("mvn:" + PEERGREEN_PUBLIC_REPOSITORY + "!" +
                                                         helloServiceJarFileNode.getData().getArtifactInfo().groupId + "/" +
                                                         helloServiceJarFileNode.getData().getArtifactInfo().artifactId + "/" +
                                                         helloServiceJarFileNode.getData().getArtifactInfo().version);
        Assert.assertEquals(helloServiceJarFileNode.getData().getUri(), expectedHelloServiceJarFileNodeURI);
    }

    @Test
    public void testSearchingByGroupId() {
        IndexerGraph<MavenNode> graph = mavenRepositoryService.list(groupId("com.peergreen.community"));
        Assert.assertTrue(graph.getNodes().size() >= 1);
        IndexerNode<MavenNode> rootNode = (IndexerNode<MavenNode>) graph.getNodes().iterator().next();
        Assert.assertTrue(rootNode.getChildren().size() >= 1);
        IndexerNode<MavenNode> peergreenNode = (IndexerNode<MavenNode>) rootNode.getChildren().get(0).getChildren().get(0);
        Assert.assertTrue(peergreenNode.getNode("example") == null);
        IndexerNode<MavenNode> communityNode = (IndexerNode<MavenNode>) peergreenNode.getChildren().get(0);
        Assert.assertEquals(communityNode.getData().getName(), "community");
    }

    @Test
    public void testSearchingByArtifactId() {
        IndexerGraph<MavenNode> graph = mavenRepositoryService.list(artifactId("peergreen-kernel-api"));
        IndexerNode<MavenNode> communityNode = (IndexerNode<MavenNode>) graph.getNodes().iterator().next().getChildren().get(0).getChildren().get(0).getChildren().get(0);
        Assert.assertEquals(communityNode.getData().getName(), "community");
        Assert.assertEquals(communityNode.getChildren().size(), 1);
        Assert.assertEquals(communityNode.getChildren().get(0).getData().getName(), "peergreen-kernel-api");
    }

    @Test
    public void testSearchingByMultipleCriteria() {
        IndexerGraph<MavenNode> graph = mavenRepositoryService.list(groupId("com.peergreen.community"), artifactId("peergreen-server-tomcat"));
        Assert.assertNotNull(graph);
        IndexerNode<MavenNode> communityNode = (IndexerNode<MavenNode>) graph.getNodes().iterator().next().getChildren().get(0).getChildren().get(0).getChildren().get(0);
        Assert.assertNotNull(communityNode);
        Assert.assertEquals(communityNode.getData().getName(), "community");
        Assert.assertEquals(communityNode.getChildren().size(), 1);
        Assert.assertEquals(communityNode.getChildren().get(0).getData().getName(), "peergreen-server-tomcat");
    }

//    @Test
//    public void testSearchingByVersionRange() {
//        IndexerGraph<MavenNode> graph1 = mavenRepositoryService.list(new MavenQuery().setVersionMax("1.0.0-M1"));
//        IndexerNode<MavenNode> communityNode1 = (IndexerNode<MavenNode>) graph1.getNodes().iterator().next().getChildren().get(0).getChildren().get(0).getChildren().get(0);
//        Assert.assertEquals(communityNode1.getData().getName(), "community");
//        Assert.assertEquals(communityNode1.getChildren().size(), 1);
//        Assert.assertEquals(communityNode1.getChildren().get(0).getData().getName(), "peergreen-server-light");
//
//        IndexerGraph<MavenNode> graph2 = mavenRepositoryService.list(new MavenQuery().setVersionMin("1.0.0"));
//        IndexerNode<MavenNode> peergreenNode = (IndexerNode<MavenNode>) graph2.getNodes().iterator().next().getChildren().get(0).getChildren().get(0);
//        IndexerNode<MavenNode> exampleNode = peergreenNode.getNode("example");
//        Assert.assertTrue(exampleNode != null);
//        IndexerNode<MavenNode> paxexamNode = exampleNode.getNode("paxexam");
//        Assert.assertEquals(paxexamNode.getData().getName(), "paxexam");
//        Assert.assertTrue(paxexamNode.getNode("paxexam-hello-service") != null);
//    }
}
