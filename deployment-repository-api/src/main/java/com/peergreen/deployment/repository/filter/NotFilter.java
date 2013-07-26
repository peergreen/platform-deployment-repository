package com.peergreen.deployment.repository.filter;

import com.peergreen.deployment.repository.search.Occur;

/**
 * @author Mohammed Boukada
 */
public class NotFilter extends AbsFilter implements Filter {

    public NotFilter(Filter[] filters) {
        super(filters);
    }

    @Override
    public Occur getOccurrence() {
        return Occur.MUST_NOT;
    }
}
