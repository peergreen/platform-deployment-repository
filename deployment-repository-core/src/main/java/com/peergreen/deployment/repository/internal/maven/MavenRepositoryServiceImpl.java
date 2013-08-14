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
import com.peergreen.deployment.repository.internal.search.BaseQueryVisitor;
import com.peergreen.deployment.repository.maven.MavenArtifactInfo;
import com.peergreen.deployment.repository.maven.MavenNode;
import com.peergreen.deployment.repository.MavenRepositoryService;
import com.peergreen.deployment.repository.internal.base.AttributesName;
import com.peergreen.deployment.repository.BaseNode;
import com.peergreen.deployment.repository.internal.base.InternalAttributes;
import com.peergreen.deployment.repository.internal.tree.IndexerGraph;
import com.peergreen.deployment.repository.internal.tree.IndexerNode;
import com.peergreen.deployment.repository.search.RepositoryQuery;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.Indexer;
import org.apache.maven.index.IteratorSearchRequest;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.updater.IndexUpdateRequest;
import org.apache.maven.index.updater.IndexUpdateResult;
import org.apache.maven.index.updater.IndexUpdater;
import org.apache.maven.index.updater.ResourceFetcher;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mohammed Boukada
 */
@Component(name = "com.peergreen.deployment.repository.maven")
@Provides(properties = @StaticServiceProperty(name = "repository.type", type = "java.lang.String", mandatory = true))
public class MavenRepositoryServiceImpl implements MavenRepositoryService {

    @Property(name = "repository.name")
    private String name;
    @Property(name = "repository.url", mandatory = true)
    private String url;

    private PlexusContainer plexusContainer;
    private Indexer indexer;
    private IndexUpdater indexUpdater;
    private IndexingContext context;

    @Validate
    public void init() throws PlexusContainerException, ComponentLookupException, IOException {
        plexusContainer = new DefaultPlexusContainer();
        indexer = plexusContainer.lookup(Indexer.class);
        indexUpdater = plexusContainer.lookup(IndexUpdater.class);

        if (url.charAt(url.length() - 1) == '/') {
            url = url.substring(0, url.length() - 1);
        }
        URL urlObject = new URL(url);
        File repoLocalCache = new File("target/" + urlObject.getHost() + "/cache");
        File repoIndexDir = new File("target/" + urlObject.getHost() + "/index");
        List<IndexCreator> indexers = new ArrayList<IndexCreator>();
        indexers.add( plexusContainer.lookup( IndexCreator.class, "min" ) );
        indexers.add( plexusContainer.lookup( IndexCreator.class, "jarContent" ) );
        indexers.add( plexusContainer.lookup( IndexCreator.class, "maven-plugin" ) );
        context = indexer.createIndexingContext("context", urlObject.getHost(), repoLocalCache, repoIndexDir, url, null, true, true, indexers);
        IndexUpdateRequest updateRequest = new IndexUpdateRequest(context, new ResourceFetcher() {
            @Override
            public void connect(String id, String url) throws IOException {

            }

            @Override
            public void disconnect() throws IOException {
            }

            @Override
            public InputStream retrieve(String name) throws IOException, FileNotFoundException {
                return new URL(url + File.separator + ".index" + File.separator + name).openStream();
            }
        });

        Date contextCurrentTimestamp = context.getTimestamp();
        IndexUpdateResult updateResult = indexUpdater.fetchAndUpdateIndex(updateRequest);
        if (updateResult.isFullUpdate()) {
            System.out.println( "Full update happened!" );
        } else if (updateResult.getTimestamp().equals(contextCurrentTimestamp)) {
            System.out.println( "No update needed, index is up to date!" );
        } else {
            System.out.println( "Incremental update happened, change covered " + contextCurrentTimestamp
                    + " - " + updateResult.getTimestamp() + " period." );
        }
    }

    @Invalidate
    public void stop() throws IOException {
        closeIndexingContext();
    }

    @Override
    public IndexerGraph<BaseNode> list(String filter) {
        IndexerGraph<BaseNode> graph = new IndexerGraph<>();
        try {
            // add root element
            BaseNode root = new BaseNode(name, new URI(url), false);
            IndexerNode<BaseNode> rootNode = new IndexerNode<BaseNode>(root);
            graph.addNode(rootNode);

            for (ArtifactInfo artifactInfo : process(createQuery(filter))) {
                // add GroupId
                String[] splitGroupId = splitGroupId(artifactInfo.groupId);
                IndexerNode<BaseNode> currentNode = rootNode;
                for (String aSplitGroupId : splitGroupId) {
                    IndexerNode<BaseNode> node = currentNode.getNode(aSplitGroupId);
                    if (node != null) {
                        currentNode = node;
                    } else {
                        URI newNodeUri = new URI(url + "/" + aSplitGroupId);
                        BaseNode newNodeData = new BaseNode(aSplitGroupId, newNodeUri, false);
                        IndexerNode<BaseNode> newNode = new IndexerNode<BaseNode>(newNodeData);
                        currentNode.addChild(newNode);
                        currentNode = newNode;
                    }
                }

                // add ArtifactId
                IndexerNode<BaseNode> artifactNode = currentNode.getNode(artifactInfo.artifactId);
                if (artifactNode != null) {
                    currentNode = artifactNode;
                } else {
                    URI newNodeUri = new URI(currentNode.getData().getUri().toString() + "/" + artifactInfo.artifactId);
                    BaseNode newNodeData = new BaseNode(artifactInfo.artifactId, newNodeUri, false);
                    IndexerNode<BaseNode> newNode = new IndexerNode<BaseNode>(newNodeData);
                    currentNode.addChild(newNode);
                    currentNode = newNode;
                }

                // add Version
                IndexerNode<BaseNode> versionNode = currentNode.getNode(artifactInfo.version);
                if (versionNode != null) {
                    currentNode = versionNode;
                } else {
                    URI newNodeUri = new URI(currentNode.getData().getUri().toString() + "/" + artifactInfo.version);
                    BaseNode newNodeData = new BaseNode(artifactInfo.version, newNodeUri, false);
                    IndexerNode<BaseNode> newNode = new IndexerNode<BaseNode>(newNodeData);
                    currentNode.addChild(newNode);
                    currentNode = newNode;
                }

                // Add file
                StringBuilder filename = new StringBuilder();
                filename.append(artifactInfo.artifactId);
                filename.append("-");
                filename.append(artifactInfo.version);
                if (artifactInfo.classifier != null) {
                    filename.append("-");
                    filename.append(artifactInfo.classifier);
                }
                filename.append(".");
                filename.append(artifactInfo.fextension);

                IndexerNode<BaseNode> fileNode = currentNode.getNode(filename.toString());
                if (fileNode == null) {
                    URI newNodeUri = new URI(artifactInfo.remoteUrl);
                    BaseNode newNodeData = new BaseNode(filename.toString(), newNodeUri, true);
                    IndexerNode<BaseNode> newNode = new IndexerNode<BaseNode>(newNodeData);
                    currentNode.addChild(newNode);
                }
            }
        } catch (IOException | URISyntaxException e) {
            // do nothing return empty graph
        }
        return graph;
    }

    @Override
    public IndexerGraph<MavenNode> list(com.peergreen.deployment.repository.search.Query... queries) {
        IndexerGraph<MavenNode> graph = new IndexerGraph<>();
        try {
            // add root element
            MavenNode root = new MavenNode(name, new URI(url), false, "repository", new MavenArtifactInfo(url, null, null, null, null));
            IndexerNode<MavenNode> rootNode = new IndexerNode<MavenNode>(root);
            graph.addNode(rootNode);

            IteratorSearchResponse response = process(createQuery(queries));
            for (ArtifactInfo artifactInfo : response.getResults()) {
                // add GroupId
                String[] splitGroupId = splitGroupId(artifactInfo.groupId);
                IndexerNode<MavenNode> currentNode = rootNode;
                for (int i = 0; i < splitGroupId.length; i++) {
                    IndexerNode<MavenNode> node = currentNode.getNode(splitGroupId[i]);
                    if (node != null) {
                        currentNode = node;
                    } else {
                        URI newNodeUri = new URI(currentNode.getData().getUri().toString() + "/" + splitGroupId[i]);
                        MavenArtifactInfo mavenArtifactInfo = new MavenArtifactInfo(url, getSubGroupId(splitGroupId, i), null, null, null);
                        MavenNode newNodeData = new MavenNode(splitGroupId[i], newNodeUri, false, ArtifactInfo.GROUP_ID, mavenArtifactInfo);
                        IndexerNode<MavenNode> newNode = new IndexerNode<MavenNode>(newNodeData);
                        currentNode.addChild(newNode);
                        currentNode = newNode;
                    }
                }
                currentNode.getData().setArtifactInfo(new MavenArtifactInfo(url, artifactInfo.groupId, null, null, null));

                // add ArtifactId
                IndexerNode<MavenNode> artifactNode = currentNode.getNode(artifactInfo.artifactId);
                if (artifactNode != null) {
                    currentNode = artifactNode;
                } else {
                    URI newNodeUri = new URI(currentNode.getData().getUri().toString() + "/" + artifactInfo.artifactId);
                    MavenArtifactInfo mavenArtifactInfo = new MavenArtifactInfo(url, artifactInfo.groupId, artifactInfo.artifactId, null, null);
                    MavenNode newNodeData = new MavenNode(artifactInfo.artifactId, newNodeUri, false, ArtifactInfo.ARTIFACT_ID, mavenArtifactInfo);
                    IndexerNode<MavenNode> newNode = new IndexerNode<MavenNode>(newNodeData);
                    currentNode.addChild(newNode);
                    currentNode = newNode;
                }

                // add Version
                IndexerNode<MavenNode> versionNode = currentNode.getNode(artifactInfo.version);
                if (versionNode != null) {
                    currentNode = versionNode;
                } else {
                    URI newNodeUri = new URI(currentNode.getData().getUri().toString() + "/" + artifactInfo.version);
                    MavenArtifactInfo mavenArtifactInfo = new MavenArtifactInfo(url, artifactInfo.groupId, artifactInfo.artifactId, artifactInfo.version, null);
                    MavenNode newNodeData = new MavenNode(artifactInfo.version, newNodeUri, false, ArtifactInfo.VERSION, mavenArtifactInfo);
                    IndexerNode<MavenNode> newNode = new IndexerNode<MavenNode>(newNodeData);
                    currentNode.addChild(newNode);
                    currentNode = newNode;
                }

                // Add file
                StringBuilder filename = new StringBuilder();
                filename.append(artifactInfo.artifactId);
                filename.append("-");
                filename.append(artifactInfo.version);
                if (artifactInfo.classifier != null) {
                    filename.append("-");
                    filename.append(artifactInfo.classifier);
                }
                filename.append(".");
                filename.append(artifactInfo.fextension);

                IndexerNode<MavenNode> fileNode = currentNode.getNode(filename.toString());
                if (fileNode == null) {
                    URI newNodeUri = new URI(currentNode.getData().getUri().toString() + "/" + filename.toString());
                    MavenArtifactInfo mavenArtifactInfo = new MavenArtifactInfo(url, artifactInfo.groupId, artifactInfo.artifactId, artifactInfo.version, artifactInfo.classifier);
                    MavenNode newNodeData = new MavenNode(filename.toString(), newNodeUri, true, "file", mavenArtifactInfo);
                    IndexerNode<MavenNode> newNode = new IndexerNode<MavenNode>(newNodeData);
                    currentNode.addChild(newNode);
                }
            }
            response.close();
        } catch (IOException | URISyntaxException e) {
            // do nothing return empty graph
        }
        return graph;
    }

    private String getSubGroupId(String[] splitGroupId, int index) {
        StringBuilder sb = new StringBuilder();
        for (int i= 0; i <= index; i++) {
            sb.append(splitGroupId[i]);
            sb.append('.');
        }
        sb.append('*');
        return sb.toString();
    }

    private Query createQuery(String filter) {
        BooleanQuery query = new BooleanQuery();
        String f = wildCardFilter(filter);
        Query q = new WildcardQuery(new Term(ArtifactInfo.GROUP_ID, f));
        query.add(q, BooleanClause.Occur.SHOULD);
        q = new WildcardQuery(new Term(ArtifactInfo.ARTIFACT_ID, f));
        query.add(q, BooleanClause.Occur.SHOULD);
        q = new WildcardQuery(new Term(ArtifactInfo.CLASSIFIER, f));
        query.add(q, BooleanClause.Occur.SHOULD);
        q = new WildcardQuery(new Term(ArtifactInfo.VERSION, f));
        query.add(q, BooleanClause.Occur.SHOULD);
        return query;
    }

    private String wildCardFilter(String s) {
        return "*" + s + "*";
    }

    @Override
    public Attributes getAttributes() {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(AttributesName.NAME, name);
        attributes.put(AttributesName.URL, url);
        return new InternalAttributes(attributes);
    }

    protected Query createQuery(com.peergreen.deployment.repository.search.Query[] queries) {
        BooleanQuery query = new BooleanQuery();
        // gets all packaging
        Query q = new WildcardQuery(new Term(ArtifactInfo.PACKAGING, "*"));
        query.add(q, BooleanClause.Occur.SHOULD);

        // Transform Queries
        for(com.peergreen.deployment.repository.search.Query pQuery : queries) {
            if (!(pQuery instanceof RepositoryQuery)) {
                BaseQueryVisitor visitor = new BaseQueryVisitor();
                pQuery.walk(visitor);
                query.add(visitor.getQuery(), BooleanClause.Occur.MUST);
            }
        }

        return query;
    }

    private IteratorSearchResponse process(Query query) throws IOException {
        final IteratorSearchRequest request = new IteratorSearchRequest(query, Collections.singletonList(context));
        return indexer.searchIterator(request);
    }

    private String[] splitGroupId(String groupId) {
        return groupId.split("\\.");
    }

    protected void setUrl(String url) {
        this.url = url;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void closeIndexingContext() throws IOException {
        indexer.closeIndexingContext(context, true);
    }
}
