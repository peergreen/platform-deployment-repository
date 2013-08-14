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
import com.peergreen.deployment.repository.internal.directory.FrontalDirectoryRepositoryService;
import com.peergreen.deployment.repository.internal.tree.IndexerGraph;
import com.peergreen.deployment.repository.internal.tree.IndexerNode;
import com.peergreen.deployment.repository.view.Facade;
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
public class TestFrontalDirectoryRepositoryService {
    private FrontalDirectoryRepositoryService frontalDirectoryRepositoryService;
    private DirectoryRepositoryServiceImpl directoryRepositoryService1;
    private DirectoryRepositoryServiceImpl directoryRepositoryService2;

    @BeforeClass
    public void configure() throws FileNotFoundException, NoDirectoryException, URISyntaxException {
        frontalDirectoryRepositoryService = new FrontalDirectoryRepositoryService();

        directoryRepositoryService1 = new DirectoryRepositoryServiceImpl();
        directoryRepositoryService1.setName("Test Directory");
        directoryRepositoryService1.setUrl(getClass().getResource("/testDir").toString());

        directoryRepositoryService2 = new DirectoryRepositoryServiceImpl();
        directoryRepositoryService2.setName("Test Directory 2");
        directoryRepositoryService2.setUrl(getClass().getResource("/testDir2").toString());

        frontalDirectoryRepositoryService.bindDirectoryRepositoryService(directoryRepositoryService1);
        frontalDirectoryRepositoryService.bindDirectoryRepositoryService(directoryRepositoryService2);
    }

    @Test
    public void testGetAttributes() {
        Attributes attributes = frontalDirectoryRepositoryService.getAttributes();
        Repository repository = attributes.as(Repository.class);
        Assert.assertEquals(repository.getName(), null);
        Assert.assertEquals(repository.getUrl(), null);
        Facade facade = attributes.as(Facade.class);
        Assert.assertNotNull(facade);
        Assert.assertTrue(facade.getUrls().contains(getClass().getResource("/testDir").toString()));
        Assert.assertTrue(facade.getUrls().contains(getClass().getResource("/testDir2").toString()));
    }

    @Test
    public void testFetchFilesFromTwoRepositories() {
        IndexerGraph<BaseNode> graph = frontalDirectoryRepositoryService.list("");
        Assert.assertTrue(graph.getNodes().size() == 2);
        IndexerNode<BaseNode> dir1Node = graph.getNode("Test Directory");
        Assert.assertNotNull(dir1Node);
        Assert.assertEquals(dir1Node.getChildren().size(), 4);
        IndexerNode<BaseNode> dir2Node = graph.getNode("Test Directory 2");
        Assert.assertNotNull(dir2Node);
        Assert.assertEquals(dir2Node.getChildren().size(), 1);
    }
}
