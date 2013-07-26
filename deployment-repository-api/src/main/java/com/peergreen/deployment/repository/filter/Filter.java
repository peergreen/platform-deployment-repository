package com.peergreen.deployment.repository.filter;

import com.peergreen.deployment.repository.search.Occur;

import java.util.List;

/**
 * @author Mohammed Boukada
 */
public interface Filter extends Iterable<Filter> {
    List<Filter> getSubFilters();
    Occur getOccurrence();
}
