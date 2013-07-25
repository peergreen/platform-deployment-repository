/*
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.deployment.repository.maven;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.maven.index.ArtifactInfo;

import java.util.Collection;

/**
 * @author Mohammed Boukada
 */
public class MavenDSLQuery extends AbstractDSLQuery {

    private String groupId;
    private String artifactId;
    private String versionMin;
    private String versionMax;
    private String classifier;

    public MavenDSLQuery setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public MavenDSLQuery setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public MavenDSLQuery setVersion(String version) {
        this.versionMin = version;
        this.versionMax = version;
        return this;
    }

    public MavenDSLQuery setVersionMin(String versionMin) {
        this.versionMin = versionMin;
        return this;
    }

    public MavenDSLQuery setVersionMax(String versionMax) {
        this.versionMax = versionMax;
        return this;
    }

    public MavenDSLQuery setVersionRange(String versionMin, String versionMax) {
        this.versionMin = versionMin;
        this.versionMax = versionMax;
        return this;
    }

    public MavenDSLQuery setClassifier(String classifier) {
        this.classifier = classifier;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersionMin() {
        return versionMin;
    }

    public String getVersionMax() {
        return versionMax;
    }

    public String getClassifier() {
        return classifier;
    }

    public Query getQuery() {
        if (getGroupId() != null || getArtifactId() != null || getClassifier() != null
                || getVersionMin() != null || getVersionMax() != null) {
            BooleanQuery query = new BooleanQuery();
            if (getGroupId() != null) {
                Query q = new WildcardQuery(new Term(ArtifactInfo.GROUP_ID, getGroupId()));
                query.add(q, BooleanClause.Occur.MUST);
            }
            if (getArtifactId() != null) {
                Query q = new WildcardQuery(new Term(ArtifactInfo.ARTIFACT_ID, getArtifactId()));
                query.add(q, BooleanClause.Occur.MUST);
            }
            if (getClassifier() != null) {
                Query q = new WildcardQuery(new Term(ArtifactInfo.CLASSIFIER, getClassifier()));
                query.add(q, BooleanClause.Occur.MUST);
            }
            if (getVersionMin() != null || getVersionMax() != null) {
                if (getVersionMin() != null && getVersionMax() != null) {
                    if (getVersionMax().equals(getVersionMin())) {
                        Query q = new WildcardQuery(new Term(ArtifactInfo.VERSION, getVersionMax()));
                        query.add(q, BooleanClause.Occur.MUST);
                    } else {
                        Query q = new TermRangeQuery(ArtifactInfo.VERSION, getVersionMin(), getVersionMax(), true, true);
                        query.add(q, BooleanClause.Occur.MUST);
                    }
                } else if (getVersionMin() != null && getVersionMax() == null) {
                    Query q = new TermRangeQuery(ArtifactInfo.VERSION, getVersionMin(), null, true, true);
                    query.add(q, BooleanClause.Occur.MUST);
                } else if (getVersionMin() == null && getVersionMax() != null) {
                    Query q = new TermRangeQuery(ArtifactInfo.VERSION, null, getVersionMax(), true, true);
                    query.add(q, BooleanClause.Occur.MUST);
                }
            }
            return query;
        }
        return null;
    }

//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        if (getGroupId() != null || getArtifactId() != null || getClassifier() != null
//                || getVersionMin() != null || getVersionMax() != null) {
//            //sb.append('(');
//            if (getGroupId() != null) {
//                sb.append('+');
//                sb.append(ArtifactInfo.GROUP_ID);
//                sb.append(':');
//                sb.append(escape(getGroupId()));
//                sb.append(addOperator(BooleanOperator.AND));
//            }
//            if (getArtifactId() != null) {
//                sb.append('+');
//                sb.append(ArtifactInfo.ARTIFACT_ID);
//                sb.append(':');
//                sb.append(escape(getArtifactId()));
//                sb.append(addOperator(BooleanOperator.AND));
//            }
//            if (getClassifier() != null) {
//                sb.append('+');
//                sb.append(ArtifactInfo.CLASSIFIER);
//                sb.append(':');
//                sb.append(escape(getClassifier()));
//                sb.append(addOperator(BooleanOperator.AND));
//            }
//            if (getVersionMin() != null || getVersionMax() != null) {
//                sb.append('+');
//                sb.append(ArtifactInfo.VERSION);
//                sb.append(':');
//                if (getVersionMin() != null && getVersionMax() != null) {
//                    if (getVersionMax().equals(getVersionMin())) {
//                        sb.append(escape(getVersionMax()));
//                    } else {
//                        sb.append('[');
//                        sb.append(escape(getVersionMin()));
//                        sb.append(" TO ");
//                        sb.append(escape(getVersionMax()));
//                        sb.append(']');
//                    }
//                } else if (getVersionMin() != null && getVersionMax() == null) {
//                    sb.append('[');
//                    sb.append(escape(getVersionMin()));
//                    sb.append(" TO *]");
//                } else if (getVersionMin() == null && getVersionMax() != null) {
//                    sb.append("[* TO ");
//                    sb.append(escape(getVersionMax()));
//                    sb.append(']');
//                }
//                sb.append(addOperator(BooleanOperator.AND));
//            }
//            sb.delete(sb.length() - 5, sb.length());
//            //sb.append(')');
//        }
//        return sb.toString();
//    }
//
//    private String addOperator(String operator) {
//        return ' ' + operator + ' ';
//    }
//
//    private String escape(String s) {
//        return '"' + QueryParser.escape(s) + '"';
//    }
}
