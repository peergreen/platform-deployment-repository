package com.peergreen.deployment.repository.search;

import com.peergreen.deployment.repository.maven.MavenArtifactInfo;

/**
 * @author Mohammed Boukada
 */
public class AttributeQuery implements Query {
    private MavenArtifactInfo.Type field;
    private String value;
    private Occur occur;

    public AttributeQuery(MavenArtifactInfo.Type field, String value, Occur occur) {
        this.field = field;
        this.value = value;
        this.occur = occur;
    }

    @Override
    public void walk(QueryVisitor visitor) {
        visitor.visitAttributeQuery(field, value, occur);
    }
}
