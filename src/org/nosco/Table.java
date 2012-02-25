package org.nosco;

import java.sql.SQLException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.nosco.Field.PK;



/**
 * This is the base class of all classes generated by this API.
 * Some of these methods are public only by necessity.  Please use only
 * {@code insert()}, {@code update()}, {@code save()} and {@code dirty()}.
 * I consider all others fair game for changing in later versions of the API. 
 * 
 * @author Derek Anderson
 */
public abstract class Table {

	/**
	 * Please do not use.
	 * @return
	 */
	public abstract String SCHEMA_NAME();

	/**
	 * Please do not use.
	 * @return
	 */
	public abstract String TABLE_NAME();

	/**
	 * Please do not use.
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public abstract Field[] FIELDS();

	/**
	 * Please do not use.
	 * @return
	 */
	public abstract Field.PK PK();

	/**
	 * Please do not use.
	 * @return
	 */
	public abstract Field.FK[] FKS();

	/**
	 * Please do not use.
	 */
	protected BitSet __NOSCO_FETCHED_VALUES = new BitSet();

	/**
	 * Please do not use.
	 */
protected BitSet __NOSCO_UPDATED_VALUES = null;
	protected boolean __NOSCO_GOT_FROM_DATABASE = false;

	/**
	 * Returns true if the object has been modified
	 * @return true if the object has been modified
	 */
	public boolean dirty() {
		return __NOSCO_UPDATED_VALUES != null && !__NOSCO_UPDATED_VALUES.isEmpty();
	}

	/**
	 * Creates and executes an insert statement for this object 
	 * (irregardless of if it's already in the database)
	 * @return success
	 * @throws SQLException
	 */
	public abstract boolean insert() throws SQLException;

	/**
	 * Creates and executes an update statement for this object 
	 * (irregardless of if it's already in the database)
	 * @return success
	 * @throws SQLException
	 */
	public abstract boolean update() throws SQLException;

	/**
	 * Creates and executes an insert or update statement for this object 
	 * based on if the object came from the database or not.
	 * @return success
	 * @throws SQLException
	 */
	public abstract boolean save() throws SQLException;

	static Map<Table,java.lang.reflect.Field> _pkCache = new HashMap<Table, java.lang.reflect.Field>();
	static Field.PK GET_TABLE_PK(Table table) {
		if (!_pkCache.containsKey(table)) {
			java.lang.reflect.Field field = null;
			try {
				field = table.getClass().getDeclaredField("PK");
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
			_pkCache.put(table, field);
		}
		try {
			return (PK) _pkCache.get(table).get(table);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Please do not use.
	 * @return
	 */
	public boolean sameTable(Table t) {
		return t.SCHEMA_NAME() == SCHEMA_NAME() && t.TABLE_NAME() == TABLE_NAME();
	}

}
