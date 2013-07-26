package com.peergreen.deployment.repository.search;

import com.peergreen.deployment.repository.maven.MavenArtifactInfo;

/**
 * @author Mohammed Boukada
 */
public interface QueryVisitor {
    void visitAttributeQuery(MavenArtifactInfo.Type field, String value, Occur occur);
    void visitRangeQuery(MavenArtifactInfo.Type field, String minValue, String maxValue, boolean includeMin, boolean includeMax);
    QueryVisitor visitCompositeQuery();
    void visitEnd();
}
