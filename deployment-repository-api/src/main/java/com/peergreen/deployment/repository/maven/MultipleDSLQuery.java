package com.peergreen.deployment.repository.maven;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

/**
 * @author Mohammed Boukada
 */
public class MultipleDSLQuery extends AbstractDSLQuery {
    private Query query;

    public MultipleDSLQuery and(AbstractDSLQuery op1, AbstractDSLQuery op2) {
        BooleanQuery query = new BooleanQuery();
        query.add(op1.getQuery(), BooleanClause.Occur.MUST);
        query.add(op2.getQuery(), BooleanClause.Occur.MUST);
        this.query = query;
        return this;
    }

    public MultipleDSLQuery or(AbstractDSLQuery op1, AbstractDSLQuery op2) {
        BooleanQuery query = new BooleanQuery();
        query.add(op1.getQuery(), BooleanClause.Occur.SHOULD);
        query.add(op2.getQuery(), BooleanClause.Occur.SHOULD);
        this.query = query;
        return this;
    }

    public MultipleDSLQuery not(AbstractDSLQuery op) {
        BooleanQuery query = new BooleanQuery();
        query.add(op.getQuery(), BooleanClause.Occur.MUST_NOT);
        this.query = query;
        return this;
    }

    public Query getQuery() {
        return query;
    }
}
