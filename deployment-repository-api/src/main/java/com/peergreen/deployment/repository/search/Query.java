package com.peergreen.deployment.repository.search;

/**
 * @author Mohammed Boukada
 */
public interface Query {
    void walk(QueryVisitor visitor);
}