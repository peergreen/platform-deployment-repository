package com.peergreen.deployment.repository.filter;

import com.peergreen.deployment.repository.search.Occur;

/**
 * @author Mohammed Boukada
 */
public class OrFilter extends AbsFilter implements Filter {

    public OrFilter(Filter[] filters) {
        super(filters);
    }

    @Override
    public Occur getOccurrence() {
        return Occur.SHOULD;
    }
}
