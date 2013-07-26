package com.peergreen.deployment.repository.maven;

import com.peergreen.deployment.repository.filter.Filter;
import com.peergreen.deployment.repository.filter.StringFilter;
import com.peergreen.deployment.repository.filter.VersionFilter;
import com.peergreen.deployment.repository.search.AttributeQuery;
import com.peergreen.deployment.repository.search.Occur;
import com.peergreen.deployment.repository.search.Query;
import com.peergreen.deployment.repository.search.RangeQuery;

/**
 * @author Mohammed Boukada
 */
public class VersionQuery extends AbstractMavenQuery {

    private VersionFilter versionFilter;

    public VersionQuery(String groupId) {
        super(groupId);
    }

    public VersionQuery(Filter filter) {
        super(filter);
    }

    public VersionQuery(VersionFilter versionFilter) {
        this.versionFilter = versionFilter;
    }

    @Override
    public Query getQuery() {
        if (versionFilter != null) {
            return createRangeQuery(versionFilter);
        }
        return super.getQuery();
    }

    @Override
    protected Query createAttributeQuery(StringFilter filter, Occur occur) {
        return new AttributeQuery(MavenArtifactInfo.Type.VERSION, filter.getValue(), occur);
    }

    private Query createRangeQuery(VersionFilter versionFilter) {
        return new RangeQuery(MavenArtifactInfo.Type.VERSION, versionFilter.getMinValue(), versionFilter.getMaxValue(),
                versionFilter.isMinIncluded(), versionFilter.isMaxIncluded());
    }
}
