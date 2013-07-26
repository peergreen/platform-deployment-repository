package com.peergreen.deployment.repository.maven;

import com.peergreen.deployment.repository.search.AttributeQuery;
import com.peergreen.deployment.repository.search.Occur;
import com.peergreen.deployment.repository.search.Query;
import com.peergreen.deployment.repository.filter.Filter;
import com.peergreen.deployment.repository.filter.StringFilter;

/**
 * @author Mohammed Boukada
 */
public class GroupIdQuery extends AbstractMavenQuery {

    public GroupIdQuery(String groupId) {
        super(groupId);
    }

    public GroupIdQuery(Filter filter) {
        super(filter);
    }

    @Override
    protected Query createAttributeQuery(StringFilter filter, Occur occur) {
        return new AttributeQuery(MavenArtifactInfo.Type.GROUP_ID, filter.getValue(), occur);
    }
}
