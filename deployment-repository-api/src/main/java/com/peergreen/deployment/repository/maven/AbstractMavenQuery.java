package com.peergreen.deployment.repository.maven;

import com.peergreen.deployment.repository.filter.Filter;
import com.peergreen.deployment.repository.filter.StringFilter;
import com.peergreen.deployment.repository.search.CompositeQuery;
import com.peergreen.deployment.repository.search.Occur;
import com.peergreen.deployment.repository.search.Query;

/**
 * @author Mohammed Boukada
 */
public abstract class AbstractMavenQuery {
    private Filter filter;

    protected AbstractMavenQuery() {
    }

    public AbstractMavenQuery(String groupId) {
        this(new StringFilter(groupId));
    }

    public AbstractMavenQuery(Filter filter) {
        this.filter = filter;
    }

    public Query getQuery() {
        return createQuery(filter, Occur.MUST);
    }

    private Query createQuery(Filter filter, Occur occur) {
        if (filter instanceof StringFilter) {
            return createAttributeQuery((StringFilter) filter, occur);
        } else {
            CompositeQuery query = new CompositeQuery();
            Occur operation = filter.getOccurrence();
            for (Filter subFilter : filter.getSubFilters()) {
                query.add(createQuery(subFilter, operation));
            }
            return query;
        }
    }

    protected abstract Query createAttributeQuery(StringFilter filter, Occur occur);
}
