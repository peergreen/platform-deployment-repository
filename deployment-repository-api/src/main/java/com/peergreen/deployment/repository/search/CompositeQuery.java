package com.peergreen.deployment.repository.search;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mohammed Boukada
 */
public class CompositeQuery implements Query {
    private List<Query> queries = new ArrayList<>();

    public void add(Query query) {
        queries.add(query);
    }

    @Override
    public void walk(QueryVisitor visitor) {
        QueryVisitor compositeVisitor = visitor.visitCompositeQuery();
        for (Query query : queries) {
            query.walk(compositeVisitor);
        }
        visitor.visitEnd();
    }
}
