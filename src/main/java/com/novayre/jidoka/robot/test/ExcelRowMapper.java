package com.novayre.jidoka.robot.test;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.util.CellReference;

import com.novayre.jidoka.data.provider.api.IExcel;
import com.novayre.jidoka.data.provider.api.IRowMapper;

/**
 * Class to map an Excel file to the POJO class representing the object
 * {@link ExcelRow} and vice versa.
 * 
 * @author Jidoka
 */
public class ExcelRowMapper implements IRowMapper<IExcel, ExcelRow> {

	/**
	 * First column.
	 */
	private static final int COL_1 = 0;

	/**
	 * Column with the result.
	 */
	private static final int RESULT_COL = 1;

	/**
	 * @see IRowMapper#map(Object, int)
	 */
	@Override
	public ExcelRow map(IExcel data, int rowNum) {

		ExcelRow r = new ExcelRow();

		r.setCol1(data.getCellValueAsString(rowNum, COL_1));
		r.setResult(data.getCellValueAsString(rowNum, RESULT_COL));

		return isLastRow(r) ? null : r;
	}

	/**
	 * @see IRowMapper#update(Object, int, Object)
	 */
	@Override
	public void update(IExcel data, int rowNum, ExcelRow rowData) {

		data.setCellValueByRef(new CellReference(rowNum, COL_1), rowData.getCol1());
		data.setCellValueByRef(new CellReference(rowNum, RESULT_COL), rowData.getResult());

		// Auto size the column
		for (int i = 1; i < 3; i++) {
			data.getSheet().autoSizeColumn(i);
		}
	}

	/**
	 * The last row is determined when the first row without content in the first
	 * column is detected.
	 * <p>
	 * Another possibility could be to check also the second and the third columns.
	 * 
	 * @see IRowMapper#isLastRow(Object)
	 */
	@Override
	public boolean isLastRow(ExcelRow instance) {
		return instance == null || StringUtils.isBlank(instance.getCol1());
	}
}
