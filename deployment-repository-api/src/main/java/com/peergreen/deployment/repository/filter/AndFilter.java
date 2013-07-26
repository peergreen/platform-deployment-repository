package com.peergreen.deployment.repository.filter;

import com.peergreen.deployment.repository.search.Occur;

/**
 * @author Mohammed Boukada
 */
public class AndFilter extends AbsFilter implements Filter {

    public AndFilter(Filter[] filters) {
        super(filters);
    }

    @Override
    public Occur getOccurrence() {
        return Occur.MUST;
    }
}
