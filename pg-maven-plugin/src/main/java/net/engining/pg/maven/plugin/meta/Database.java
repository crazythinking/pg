package net.engining.pg.maven.plugin.meta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.engining.pg.support.db.DbType;

/**
 * 数据库设计，ERM或PDM对应的对象
 * 
 * @author Eric Lu
 *
 */
public class Database {

	private DbType dbType;
	private File source;
	private List<Table> tables = new ArrayList<Table>();
	private List<Relationship> relationships = new ArrayList<Relationship>();
	private List<Domain> domains = new ArrayList<Domain>();
	private List<String> sequences = new ArrayList<String>();

	/**
	 * @return the dbType
	 */
	public DbType getDbType() {
		return dbType;
	}

	/**
	 * @param dbType
	 *            the dbType to set
	 */
	public void setDbType(DbType dbType) {
		this.dbType = dbType;
	}

	public List<Table> getTables() {
		return tables;
	}

	public void setTables(List<Table> tables) {
		this.tables = tables;
	}

	public List<Relationship> getRelationships() {
		return relationships;
	}

	public void setRelationships(List<Relationship> relationships) {
		this.relationships = relationships;
	}

	public List<Domain> getDomains() {
		return domains;
	}

	public void setDomains(List<Domain> domains) {
		this.domains = domains;
	}

	public List<String> getSequences() {
		return sequences;
	}

	public void setSequences(List<String> sequences) {
		this.sequences = sequences;
	}

	public File getSource() {
		return source;
	}

	public void setSource(File source) {
		this.source = source;
	}
}
