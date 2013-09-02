package com.peergreen.deployment.repository;

import com.peergreen.deployment.repository.view.Repository;

import java.util.List;

/**
 * @author Mohammed Boukada
 */
public interface RepositoryManager {
    void addRepository(String url, String name, String type);
    void removeRepository(String url);
    List<Repository> getRepositories();
}
