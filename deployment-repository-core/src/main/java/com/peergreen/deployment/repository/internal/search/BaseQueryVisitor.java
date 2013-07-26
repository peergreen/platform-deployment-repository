package com.peergreen.deployment.repository.internal.search;

import com.peergreen.deployment.repository.maven.MavenArtifactInfo;
import com.peergreen.deployment.repository.search.Occur;
import com.peergreen.deployment.repository.search.QueryVisitor;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.maven.index.ArtifactInfo;

/**
 * @author Mohammed Boukada
 */
public class BaseQueryVisitor implements QueryVisitor {

    private BooleanQuery finalQuery;
    private BaseQueryVisitor subVisitor;

    public BaseQueryVisitor() {
        finalQuery = new BooleanQuery();
    }

    @Override
    public void visitAttributeQuery(MavenArtifactInfo.Type field, String value, Occur occur) {
        Query query = new WildcardQuery(new Term(getField(field), value));
        finalQuery.add(query, getOccur(occur));
    }

    @Override
    public void visitRangeQuery(MavenArtifactInfo.Type field, String minValue, String maxValue, boolean includeMin, boolean includeMax) {
        Query query = new TermRangeQuery(getField(field), minValue, maxValue, includeMin, includeMax);
        finalQuery.add(query, BooleanClause.Occur.MUST);
    }

    @Override
    public BaseQueryVisitor visitCompositeQuery() {
        subVisitor = new BaseQueryVisitor();
        return subVisitor;
    }

    @Override
    public void visitEnd() {
        finalQuery.add(subVisitor.getQuery(), BooleanClause.Occur.MUST);
    }

    public Query getQuery() {
        return finalQuery;
    }

    private BooleanClause.Occur getOccur(Occur occur) {
        switch (occur) {
            case MUST:
                return BooleanClause.Occur.MUST;
            case MUST_NOT:
                return BooleanClause.Occur.MUST_NOT;
            case SHOULD:
                return BooleanClause.Occur.SHOULD;
        }
        return null;
    }

    private String getField(MavenArtifactInfo.Type mai) {
        switch (mai) {
            case GROUP_ID:
                return ArtifactInfo.GROUP_ID;
            case ARTIFACT_ID:
                return ArtifactInfo.ARTIFACT_ID;
            case CLASSIFIER:
                return ArtifactInfo.CLASSIFIER;
            case VERSION:
                return ArtifactInfo.VERSION;
        }
        return null;
    }
}
