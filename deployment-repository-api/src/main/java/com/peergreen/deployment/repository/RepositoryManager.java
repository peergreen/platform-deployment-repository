package com.peergreen.deployment.repository;

import java.util.List;

import com.peergreen.deployment.repository.view.Repository;

/**
 * @author Mohammed Boukada
 */
public interface RepositoryManager {
    boolean addRepository(String url, String name, String type);
    boolean removeRepository(String url);
    List<Repository> getRepositories();
    void loadRepositoriesInCache();
}
