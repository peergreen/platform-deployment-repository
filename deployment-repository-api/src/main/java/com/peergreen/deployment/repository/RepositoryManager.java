package com.peergreen.deployment.repository;

/**
 * @author Mohammed Boukada
 */
public interface RepositoryManager {
    void addRepository(String url, String name, String type);
    void removeRepository(String url);
}
