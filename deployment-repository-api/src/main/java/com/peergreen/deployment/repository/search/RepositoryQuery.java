package com.peergreen.deployment.repository.search;

import com.peergreen.deployment.repository.filter.Filter;
import com.peergreen.deployment.repository.filter.StringFilter;
import com.peergreen.deployment.repository.search.AttributeQuery;
import com.peergreen.deployment.repository.search.Occur;
import com.peergreen.deployment.repository.search.Query;
import com.peergreen.deployment.repository.search.QueryVisitor;

/**
 * @author Mohammed Boukada
 */
public class RepositoryQuery implements Query {

    String[] repositories;

    public RepositoryQuery(String... repositories) {
        this.repositories = repositories;
    }

    public String[] getRepositories() {
        return repositories;
    }

    @Override
    public void walk(QueryVisitor visitor) {

    }
}
