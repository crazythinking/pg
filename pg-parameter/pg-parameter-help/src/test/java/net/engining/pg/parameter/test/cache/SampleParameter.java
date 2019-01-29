package net.engining.pg.parameter.test.cache;

import java.io.Serializable;
import java.util.List;

public class SampleParameter implements Serializable{

	private static final long serialVersionUID = 1L;

	public String param1;
	
	public String param2;
	
	public List<InnerParameter> paramList;
}
