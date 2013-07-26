package com.peergreen.deployment.repository.filter;

import com.peergreen.deployment.repository.search.Occur;

import java.util.Iterator;
import java.util.List;

/**
 * @author Mohammed Boukada
 */
public class StringFilter implements Filter {
    private String value;

    public StringFilter(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public List<Filter> getSubFilters() {
        return null;
    }

    @Override
    public Occur getOccurrence() {
        return null;
    }

    @Override
    public Iterator<Filter> iterator() {
        return null;
    }
}
