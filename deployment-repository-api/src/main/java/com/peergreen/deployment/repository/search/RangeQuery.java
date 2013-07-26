package com.peergreen.deployment.repository.search;

import com.peergreen.deployment.repository.maven.MavenArtifactInfo;

/**
 * @author Mohammed Boukada
 */
public class RangeQuery implements Query {

    private MavenArtifactInfo.Type field;
    private String minValue;
    private String maxValue;
    private boolean includeMin;
    private boolean includeMax;

    public RangeQuery(MavenArtifactInfo.Type field, String minValue, String maxValue, boolean includeMin, boolean includeMax) {
        this.field = field;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.includeMin = includeMin;
        this.includeMax = includeMax;
    }

    @Override
    public void walk(QueryVisitor visitor) {
        visitor.visitRangeQuery(field, minValue, maxValue, includeMin, includeMax);
    }
}
