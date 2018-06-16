package net.engining.pg.support.utils;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.embedded.ConnectionProperties;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseConfigurer;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.ClassUtils;

import com.google.common.base.Throwables;

/**
 * 参考 org.springframework.jdbc.datasource.embedded.H2EmbeddedDatabaseConfigurer，在url里加入了mvcc，以避免锁表的问题
 * @author binarier
 *
 */
public class H2MvccConfigurer implements EmbeddedDatabaseConfigurer {
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	@SuppressWarnings("unchecked")
	@Override
	public void configureConnectionProperties(ConnectionProperties properties, String databaseName) {
		try
		{
			properties.setDriverClass((Class<? extends Driver>) ClassUtils.forName("org.h2.Driver", H2MvccConfigurer.class.getClassLoader()));
			properties.setUrl(String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;MVCC=true", databaseName));
			properties.setUsername("sa");
			properties.setPassword("");
		}
		catch (Exception e)
		{
			Throwables.propagate(e);
		}
	}

	@Override
	public void shutdown(DataSource dataSource, String databaseName) {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Statement stmt = connection.createStatement();
			stmt.execute("SHUTDOWN");
		}
		catch (SQLException ex)
		{
			if (logger.isWarnEnabled()) {
				logger.warn("Could not shutdown embedded database", ex);
			}
		}
		finally
		{
			if (connection != null)
			{
				JdbcUtils.closeConnection(connection);
			}
		}
	}

}