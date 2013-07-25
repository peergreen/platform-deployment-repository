package com.peergreen.deployment.repository.maven;

import org.apache.lucene.search.Query;

import java.util.Collection;

/**
 * @author Mohammed Boukada
 */
public abstract class AbstractDSLQuery {
    protected Collection<String> repositoryUrls;

    public abstract Query getQuery();

    public void setRepositoryUrls(Collection<String> repositoryUrls) {
        this.repositoryUrls = repositoryUrls;
    }

    public Collection<String> getRepositoryUrls() {
        return repositoryUrls;
    }
}
