/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.deployment.repository.internal.maven;

import static com.peergreen.deployment.repository.maven.MavenArtifactInfo.Type.ARCHIVE;
import static com.peergreen.deployment.repository.maven.MavenArtifactInfo.Type.ARTIFACT_ID;
import static com.peergreen.deployment.repository.maven.MavenArtifactInfo.Type.GROUP_ID;
import static com.peergreen.deployment.repository.maven.MavenArtifactInfo.Type.REPOSITORY;
import static com.peergreen.deployment.repository.maven.MavenArtifactInfo.Type.VERSION;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceController;
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
import org.apache.maven.index.updater.IndexUpdater;
import org.apache.maven.index.updater.ResourceFetcher;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import com.peergreen.deployment.repository.Attributes;
import com.peergreen.deployment.repository.BaseNode;
import com.peergreen.deployment.repository.MavenRepositoryService;
import com.peergreen.deployment.repository.Node;
import com.peergreen.deployment.repository.RepositoryType;
import com.peergreen.deployment.repository.internal.base.AttributesName;
import com.peergreen.deployment.repository.internal.base.InternalAttributes;
import com.peergreen.deployment.repository.internal.search.BaseQueryVisitor;
import com.peergreen.deployment.repository.internal.tree.IndexerGraph;
import com.peergreen.deployment.repository.internal.tree.IndexerNode;
import com.peergreen.deployment.repository.maven.MavenArtifactInfo;
import com.peergreen.deployment.repository.maven.MavenNode;
import com.peergreen.deployment.repository.search.Queries;
import com.peergreen.deployment.repository.search.RepositoryQuery;

/**
 * @author Mohammed Boukada
 */
@Component(name = "com.peergreen.deployment.repository.maven")
@Provides(properties = @StaticServiceProperty(name = "repository.type", type = "java.lang.String", mandatory = true))
public class MavenRepositoryServiceImpl implements MavenRepositoryService {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(MavenRepositoryServiceImpl.class);

    @Property(name = "repository.name")
    private String name;
    @Property(name = "repository.url", mandatory = true)
    private String url;
    @ServiceController
    private boolean isReady = false;

    private PlexusContainer plexusContainer;
    private Indexer indexer;
    private IndexingContext context;
    private IndexerGraph<MavenNode> cache = new IndexerGraph<>();

    @Validate
    public void init() throws PlexusContainerException, ComponentLookupException, IOException {
        plexusContainer = new DefaultPlexusContainer();
        indexer = plexusContainer.lookup(Indexer.class);

        if (url.charAt(url.length() - 1) != '/') {
            url += '/';
        }
        URL urlObject = new URL(url);
        File repoLocalCache = new File("repository/" + urlObject.getHost() + "/cache");
        File repoIndexDir = new File("repository/" + urlObject.getHost() + "/index");
        List<IndexCreator> indexers = new ArrayList<IndexCreator>();
        indexers.add( plexusContainer.lookup(IndexCreator.class, "min") );
        context = indexer.createIndexingContext("context", urlObject.getHost(), repoLocalCache, repoIndexDir, url, null, true, true, indexers);
        IndexUpdateRequest updateRequest = new IndexUpdateRequest(context, new ResourceFetcher() {
            @Override
            public void connect(String id, String url) throws IOException {

            }

            @Override
            public void disconnect() throws IOException {
            }

            @Override
            public InputStream retrieve(String name) throws IOException {
                return new URL(url + ".index" + File.separator + name).openStream();
            }
        });

        MavenIndexUpdater mavenIndexUpdater = new MavenIndexUpdater(updateRequest);
        mavenIndexUpdater.start();
    }

    protected void ready() {
        updateRootGroups();
        isReady = true;
    }

    protected boolean isReady() {
        return isReady;
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
            root.setLastModified(context.getTimestamp().getTime());
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
                        URI newNodeUri = new URI(url + aSplitGroupId);
                        BaseNode newNodeData = new BaseNode(aSplitGroupId, newNodeUri, false);
                        IndexerNode<BaseNode> newNode = new IndexerNode<BaseNode>(newNodeData);
                        currentNode.addChild(newNode);
                        currentNode = newNode;
                    }
                    currentNode.getData().setLastModified(artifactInfo.lastModified);
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
                currentNode.getData().setLastModified(artifactInfo.lastModified);

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
                currentNode.getData().setLastModified(artifactInfo.lastModified);

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
                    URI newNodeUri = getMavenDeployableURI(artifactInfo);
                    BaseNode newNodeData = new BaseNode(filename.toString(), newNodeUri, true);
                    IndexerNode<BaseNode> newNode = new IndexerNode<BaseNode>(newNodeData);
                    currentNode.addChild(newNode);
                }
                currentNode.getData().setLastModified(artifactInfo.lastModified);
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
            MavenNode root = new MavenNode(name, new URI(url), false, new MavenArtifactInfo(url, null, null, null, null, REPOSITORY));
            root.setLastModified(context.getTimestamp().getTime());
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
                        MavenArtifactInfo mavenArtifactInfo = new MavenArtifactInfo(url, getSubGroupId(splitGroupId, i), null, null, null, GROUP_ID);
                        MavenNode newNodeData = new MavenNode(splitGroupId[i], newNodeUri, false, mavenArtifactInfo);
                        IndexerNode<MavenNode> newNode = new IndexerNode<MavenNode>(newNodeData);
                        currentNode.addChild(newNode);
                        currentNode = newNode;
                    }
                    currentNode.getData().setLastModified(artifactInfo.lastModified);
                }
                currentNode.getData().setArtifactInfo(new MavenArtifactInfo(url, artifactInfo.groupId, null, null, null, GROUP_ID));

                // add ArtifactId
                IndexerNode<MavenNode> artifactNode = currentNode.getNode(artifactInfo.artifactId);
                if (artifactNode != null) {
                    currentNode = artifactNode;
                } else {
                    URI newNodeUri = new URI(currentNode.getData().getUri().toString() + "/" + artifactInfo.artifactId);
                    MavenArtifactInfo mavenArtifactInfo = new MavenArtifactInfo(url, artifactInfo.groupId, artifactInfo.artifactId, null, null, ARTIFACT_ID);
                    MavenNode newNodeData = new MavenNode(artifactInfo.artifactId, newNodeUri, false, mavenArtifactInfo);
                    IndexerNode<MavenNode> newNode = new IndexerNode<MavenNode>(newNodeData);
                    currentNode.addChild(newNode);
                    currentNode = newNode;
                }
                currentNode.getData().setLastModified(artifactInfo.lastModified);

                // add Version
                IndexerNode<MavenNode> versionNode = currentNode.getNode(artifactInfo.version);
                if (versionNode != null) {
                    currentNode = versionNode;
                } else {
                    URI newNodeUri = new URI(currentNode.getData().getUri().toString() + "/" + artifactInfo.version);
                    MavenArtifactInfo mavenArtifactInfo = new MavenArtifactInfo(url, artifactInfo.groupId, artifactInfo.artifactId, artifactInfo.version, null, VERSION);
                    MavenNode newNodeData = new MavenNode(artifactInfo.version, newNodeUri, false, mavenArtifactInfo);
                    IndexerNode<MavenNode> newNode = new IndexerNode<MavenNode>(newNodeData);
                    currentNode.addChild(newNode);
                    currentNode = newNode;
                }
                currentNode.getData().setLastModified(artifactInfo.lastModified);

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
                    URI newNodeUri = getMavenDeployableURI(artifactInfo);
                    MavenArtifactInfo mavenArtifactInfo = new MavenArtifactInfo(url, artifactInfo.groupId, artifactInfo.artifactId, artifactInfo.version, artifactInfo.classifier, ARCHIVE);
                    MavenNode newNodeData = new MavenNode(filename.toString(), newNodeUri, true, mavenArtifactInfo);
                    IndexerNode<MavenNode> newNode = new IndexerNode<MavenNode>(newNodeData);
                    currentNode.addChild(newNode);
                }
                currentNode.getData().setLastModified(artifactInfo.lastModified);
            }
            response.close();
        } catch (IOException | URISyntaxException e) {
            // do nothing return empty graph
        }
        return graph;
    }

    @Override
    public List<Node<MavenNode>> getChildren(URI uri, MavenArtifactInfo.Type parentType) {
        return getChildren(uri, parentType, false);
    }

    @Override
    public List<Node<MavenNode>> getChildren(URI uri, MavenArtifactInfo.Type parentType, boolean refresh) {
        IndexerNode<MavenNode> node = cache.getNode(uri);
        if (!refresh && node != null && node.getChildren().size() > 0) {
            return node.getChildren();
        } else if (uri != null) {
            MavenArtifactInfo mavenArtifactInfo = getMavenArtifactInfo(uri, parentType);
            if (mavenArtifactInfo != null) {
                switch (parentType) {
                    case GROUP_ID:
                        try {
                            List<Node<MavenNode>> children = new ArrayList<>();
                            children.addAll(addGroupsToCache(cache.getNode(name), uri, mavenArtifactInfo.groupId));
                            if (context.getAllGroups().contains(mavenArtifactInfo.groupId)) {
                                cache.merge(list(Queries.groupId(mavenArtifactInfo.groupId)));
                                children.addAll(cache.getNode(uri).getChildren());
                            }
                            return children;
                        } catch (IOException | URISyntaxException e) {
                            // do nothing
                        }
                        break;
                    case ARTIFACT_ID:
                        cache.merge(list(Queries.groupId(mavenArtifactInfo.groupId), Queries.artifactId(mavenArtifactInfo.artifactId)));
                        return cache.getNode(uri).getChildren();
                    case VERSION:
                        cache.merge(list(Queries.groupId(mavenArtifactInfo.groupId), Queries.artifactId(mavenArtifactInfo.artifactId), Queries.version(mavenArtifactInfo.version)));
                        return cache.getNode(uri).getChildren();
                }
            }
        }
        return new ArrayList<>();
    }

    private MavenArtifactInfo getMavenArtifactInfo(URI uri, MavenArtifactInfo.Type parentType) {
        String sUri = uri.toString();
        if (sUri.charAt(sUri.length() - 1) == '/') {
            sUri = sUri.substring(0, sUri.length() - 1);
        }
        String relativeURI = sUri.substring(url.length() + 1);
        switch (parentType) {
            case REPOSITORY:
                return new MavenArtifactInfo(url, null, null, null, null, parentType);
            case GROUP_ID:
                return new MavenArtifactInfo(url, relativeURI.replace('/', '.'), null, null, null, parentType);
            case ARTIFACT_ID:
                String gid = relativeURI.substring(0, relativeURI.lastIndexOf('/')).replace('/', '.');
                String aid = relativeURI.substring(relativeURI.lastIndexOf('/') + 1);
                return new MavenArtifactInfo(url, gid, aid, null, null, parentType);
            case VERSION:
                String version = relativeURI.substring(relativeURI.lastIndexOf('/') + 1);
                relativeURI = relativeURI.substring(0, relativeURI.lastIndexOf('/'));
                gid = relativeURI.substring(0, relativeURI.lastIndexOf('/')).replace('/', '.');
                aid = relativeURI.substring(relativeURI.lastIndexOf('/') + 1);
                return new MavenArtifactInfo(url, gid, aid, version, null, parentType);
        }
        return null;
    }

    private List<Node<MavenNode>> addGroupsToCache(IndexerNode<MavenNode> root, URI uri, String groupId) throws IOException, URISyntaxException {
        IndexerNode<MavenNode> returnNode = null;
        for (String gid : context.getAllGroups()) {
            if (gid.startsWith(groupId)) {
                String[] splitGroupId = splitGroupId(gid);
                IndexerNode<MavenNode> currentNode = root;
                for (int i = 0; i < splitGroupId.length && i < splitGroupId(groupId).length + 1; i++) {
                    IndexerNode<MavenNode> node = currentNode.getNode(splitGroupId[i]);
                    if (node != null) {
                        currentNode = node;
                    } else {
                        URI newNodeUri = new URI(currentNode.getData().getUri().toString() + "/" + splitGroupId[i]);
                        MavenArtifactInfo mavenArtifactInfo = new MavenArtifactInfo(url, getSubGroupId(splitGroupId, i), null, null, null, GROUP_ID);
                        MavenNode newNodeData = new MavenNode(splitGroupId[i], newNodeUri, false, mavenArtifactInfo);
                        IndexerNode<MavenNode> newNode = new IndexerNode<MavenNode>(newNodeData);
                        currentNode.addChild(newNode);
                        currentNode = newNode;
                    }
                    if (currentNode.getData().getUri().equals(uri)) {
                        returnNode = currentNode;
                    }
                }
            }
        }
        return returnNode != null ? returnNode.getChildren() : null;
    }

    private URI getMavenDeployableURI(ArtifactInfo artifactInfo) throws URISyntaxException {
        StringBuilder sb = new StringBuilder();
        sb.append("mvn:");
        sb.append(url);
        sb.append('!');
        sb.append(artifactInfo.groupId);
        sb.append('/');
        sb.append(artifactInfo.artifactId);
        sb.append('/');
        sb.append(artifactInfo.version);
        return new URI(sb.toString());
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
        attributes.put(AttributesName.TYPE, RepositoryType.MAVEN);
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

    protected IndexerGraph<MavenNode> getCache(boolean refresh) {
        if (refresh) {
            updateRootGroups();
        }
        return cache;
    }

    private void updateRootGroups() {
        try {
            IndexerNode<MavenNode> rootNode = cache.getNode(name);
            if (rootNode == null) {
                MavenNode root = new MavenNode(name, new URI(url), false, new MavenArtifactInfo(url, null, null, null, null, REPOSITORY));
                rootNode = new IndexerNode<MavenNode>(root);
                cache.addNode(rootNode);
            }

            for (String rootGroup : context.getRootGroups()) {
                if (rootNode.getNode(rootGroup) == null) {
                    URI uri = new URI(rootNode.getData().getUri().toString() + "/" + rootGroup);
                    MavenArtifactInfo mavenArtifactInfo = new MavenArtifactInfo(url, rootGroup, null, null, null, GROUP_ID);
                    MavenNode data = new MavenNode(rootGroup, uri, false, mavenArtifactInfo);
                    IndexerNode<MavenNode> node = new IndexerNode<MavenNode>(data);
                    rootNode.addChild(node);
                }
            }
        } catch (IOException | URISyntaxException e) {
            // TODO use logger
        }
    }

    private class MavenIndexUpdater extends Thread {

        private IndexUpdateRequest updateRequest;

        public MavenIndexUpdater(IndexUpdateRequest updateRequest) {
            this.updateRequest = updateRequest;
        }

        @Override
        public void run() {
            try {
                init();
            } catch (ComponentLookupException | IOException e) {
                // TODO user logger
            }
        }

        public void init() throws ComponentLookupException, IOException {
            IndexUpdater indexUpdater = plexusContainer.lookup(IndexUpdater.class);
            LOGGER.info( "Updating Index  ''{0}'' ...", url);
            LOGGER.info( "This might take a while on first run, so please be patient!" );
            Date contextCurrentTimestamp = context.getTimestamp();
            IndexUpdateResult updateResult = indexUpdater.fetchAndUpdateIndex(updateRequest);
            if (updateResult.isFullUpdate()) {
                LOGGER.info( "Full update happened!" );
            } else if (updateResult.getTimestamp().equals(contextCurrentTimestamp)) {
                LOGGER.info( "No update needed, index is up to date!" );
            } else {
                LOGGER.info( "Incremental update happened, change covered ''{0}'' - ''{1}'' period.",
                        contextCurrentTimestamp, updateResult.getTimestamp());
            }
            ready();
        }
    }
}
