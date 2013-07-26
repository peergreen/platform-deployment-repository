package com.peergreen.deployment.repository.maven;

import com.peergreen.deployment.repository.filter.Filter;
import com.peergreen.deployment.repository.filter.StringFilter;
import com.peergreen.deployment.repository.search.AttributeQuery;
import com.peergreen.deployment.repository.search.Occur;
import com.peergreen.deployment.repository.search.Query;

/**
 * @author Mohammed Boukada
 */
public class ArtifactIdQuery extends AbstractMavenQuery {
    public ArtifactIdQuery(String artifactId) {
        super(artifactId);
    }

    public ArtifactIdQuery(Filter filter) {
        super(filter);
    }

    @Override
    protected Query createAttributeQuery(StringFilter filter, Occur occur) {
        return new AttributeQuery(MavenArtifactInfo.Type.ARTIFACT_ID, filter.getValue(), occur);
    }
}
