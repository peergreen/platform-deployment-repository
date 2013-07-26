package com.peergreen.deployment.repository.filter;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Mohammed Boukada
 */
public abstract class AbsFilter implements Filter {
    private List<Filter> subFilters;

    protected AbsFilter(Filter[] filters) {
        this.subFilters = Arrays.asList(filters);
    }

    @Override
    public List<Filter> getSubFilters() {
        return subFilters;
    }

    @Override
    public Iterator<Filter> iterator() {
        return subFilters.iterator();
    }
}
