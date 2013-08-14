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
import com.peergreen.deployment.repository.internal.directory.DirectoryRepositoryServiceImpl;
import com.peergreen.deployment.repository.internal.tree.IndexerGraph;
import com.peergreen.deployment.repository.internal.tree.IndexerNode;
import com.peergreen.deployment.repository.view.Repository;
import com.peergreen.deployment.repository.BaseNode;
import com.peergreen.deployment.repository.internal.exception.NoDirectoryException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

/**
 * @author Mohammed Boukada
 */
public class TestDirectoryRepositoryService {

    private DirectoryRepositoryServiceImpl directoryRepositoryService;

    @BeforeClass
    public void configure() throws FileNotFoundException, NoDirectoryException, URISyntaxException {
        directoryRepositoryService = new DirectoryRepositoryServiceImpl();
        directoryRepositoryService.setName("Test Directory");
        directoryRepositoryService.setUrl(getClass().getResource("/testDir").toString());
    }

    @Test
    public void testGetAttributes() {
        Attributes attributes = directoryRepositoryService.getAttributes();
        Repository repositoryInfo = attributes.as(Repository.class);
        Assert.assertNotNull(repositoryInfo);
        Assert.assertEquals(repositoryInfo.getName(), "Test Directory");
        Assert.assertEquals(repositoryInfo.getUrl(), getClass().getResource("/testDir").toString());
    }

    @Test
    public void testFetchFilesInDirectory() throws URISyntaxException {
        IndexerGraph<BaseNode> graph = directoryRepositoryService.list("");
        Assert.assertTrue(graph.getNodes().size() == 1);
        IndexerNode<BaseNode> rootNode = (IndexerNode<BaseNode>) graph.getNodes().iterator().next();
        Assert.assertEquals(rootNode.getData().getName(), "Test Directory");
        Assert.assertEquals(rootNode.getData().getUri(), getClass().getResource("/testDir/").toURI());
        Assert.assertEquals(rootNode.getChildren().size(), 4);
        Assert.assertTrue(rootNode.getNode("file1.txt") != null);
        Assert.assertTrue(rootNode.getNode("file2.txt") != null);
        Assert.assertTrue(rootNode.getNode("file3.txt") != null);
        IndexerNode<BaseNode> subDirNode = rootNode.getNode("subDir");
        Assert.assertTrue(subDirNode != null);
        Assert.assertEquals(subDirNode.getChildren().size(), 2);
        Assert.assertTrue(subDirNode.getNode("file4.html") != null);
        Assert.assertTrue(subDirNode.getNode("file5.xml") != null);
    }

    @Test
    public void testFetchFilesInDirectoryWithFilter() {
        IndexerGraph<BaseNode> graph = directoryRepositoryService.list(".*\\.txt");
        IndexerNode<BaseNode> rootNode = (IndexerNode<BaseNode>) graph.getNodes().iterator().next();
        Assert.assertTrue(rootNode.getNode("subDir") == null);
    }
}
