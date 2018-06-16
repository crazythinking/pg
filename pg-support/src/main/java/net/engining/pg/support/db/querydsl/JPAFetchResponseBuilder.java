package net.engining.pg.support.db.querydsl;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.jpa.impl.JPAQuery;

public class JPAFetchResponseBuilder<T> {

	private Range range;

	public JPAFetchResponseBuilder<T> range(Range range) {
		this.range = range;
		return this;
	}

	public FetchResponse<T> build(JPAQuery<T> jpaQuery) {

		FetchResponse<T> fr = new FetchResponse<T>();
		QueryResults<T> queryResults = null;

		if (range != null) {
			queryResults = jpaQuery.offset(range.getStart()).limit(range.getLength()).fetchResults();
			fr.setStart(range.getStart());

		} else {
			queryResults = jpaQuery.fetchResults();

		}

		fr.setRowCount(queryResults.getTotal());
		fr.setData(queryResults.getResults());

		return fr;
	}

	public FetchResponse<Map<String, Object>> buildAsMap(JPAQuery<Tuple> jpaQuery, Expression<?>... exprs) {

		FetchResponse<Map<String, Object>> fr = new FetchResponse<Map<String, Object>>();
		QueryResults<Tuple> queryResults = null;

		if (range != null) {
			queryResults = jpaQuery.offset(range.getStart()).limit(range.getLength()).fetchResults();
			fr.setStart(range.getStart());
			
		} else {
			queryResults = jpaQuery.fetchResults();
		}

		List<Map<String, Object>> converted = Lists.newArrayList();
		for (Tuple t : queryResults.getResults()) {
			Map<String, Object> map = Maps.newHashMap();
			for (Expression<?> expr : exprs) {
				String name = ((Path<?>) expr).getMetadata().getName();
				map.put(name, t.get(expr));
			}
			converted.add(map);
		}

		fr.setRowCount(queryResults.getTotal());
		fr.setData(converted);

		return fr;
	}

}
