package net.engining.pg.support.db.id.generator;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.jdbc.datasource.DataSourceUtils;

public abstract class AbstractDataSourceIdGenService extends AbstractIdGenService implements IdGenService, DisposableBean {

    protected DataSource dataSource = null;

    protected int mAllocated;

    protected long mNextId;

    protected Connection getConnection() {
        return DataSourceUtils.getConnection(getDataSource());
    }

    public void destroy() {
        dataSource = null;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
