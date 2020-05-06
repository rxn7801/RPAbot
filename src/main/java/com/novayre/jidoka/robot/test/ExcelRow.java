package com.novayre.jidoka.robot.test;

import java.io.Serializable;

/**
 * POJO class representing an Excel row.
 *
 * @author Jidoka
 */
public class ExcelRow implements Serializable {

	/** Serial. */
	private static final long serialVersionUID = 1L;

	/** The column 1. */
	private String col1;
	
	/** The result. */
	private String result;

	public String getCol1() {
		return col1;
	}

	public void setCol1(String col1) {
		this.col1 = col1;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
	
}
