package com.peergreen.deployment.repository.search;

/**
 * @author Mohammed Boukada
 */
public class RepositoryQuery implements Query {

    private String[] repositories;

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
