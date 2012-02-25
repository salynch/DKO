package org.nosco;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.nosco.Constants.DIRECTION;



/**
 * The Query interface is the center of the Nosco API. &nbsp; When you generate your JAR file 
 * (with {@code org.nosco.ant.CodeGenerator}) each object gets its own static {@code Query} instance. &nbsp; 
 * For example, {@code SomeClass} would have:
 * <pre>  {@code public static Query<SomeClass> ALL = [...]}</pre>
 * This will generally be your starting point for all queries expected to return {@code SomeClass} objects. &nbsp; 
 * For example, if you wanted to iterate over all of them you could do this:
 * <pre>  {@code for (SomeClass x : SomeClass.ALL)
 *     System.out.println(x);}</pre>
 * If you only want a specific one (assuming "id" was the primary key for the table) you could do this:
 * <pre>  {@code SomeClass x = SomeClass.ALL.get(SomeClass.ID.eq(123)))}</pre>
 * {@code SomeClass.ID} is a {@code Field<Integer>}.  All {@code Field<R>} objects contain a {@code eq(<R> x)}
 * which returns a {@code Condition} object.  (here passed into {@code Query.get(Condition... c)}) &nbsp; Similarly:
 * <pre>  {@code for (SomeClass x : SomeClass.ALL.where(SomeClass.NAME.like("%me%")))
 *     System.out.println(x);}</pre>
 * would print out all rows named like {@code "me"}.
 * @author Derek Anderson
 * @param <T> the type of object this will return
 */
public interface Query<T extends Table> extends Iterable<T> {

	/**
	 * Adds conditions to the query.  Usually conditions are created off the fields of tables.  
	 * Example: SomeClass.SOME_FIELD.eq("abc") would return a Condition.  
	 * So for a full query: SomeClass.ALL.where(SomeClass.SOME_FIELD.eq("abc"))
	 * Multiple conditions are ANDed together.
	 * @param conditions
	 * @return
	 */
	public Query<T> where(Condition... conditions);

	/**
	 * Returns the only element that matches the conditions.  If more than one match, throws a RuntimeException.
	 * Equivalent to .where(conditions).getTheOnly()
	 * @param conditions
	 * @return
	 */
	public T get(Condition... conditions);

	/**
	 * Returns the database-calculated count of the query.
	 * Does not download the objects into the JVM.
	 * Much faster than .asList().size() or counting the objects yourself.
	 * @return
	 * @throws SQLException
	 */
	public int count() throws SQLException;

	/**
	 * Same as .count()
	 * @return
	 * @throws SQLException
	 */
	public int size() throws SQLException;

	/**
	 * Excludes elements that match the conditions.
	 * Equivalent to: .where(condition.not())
	 * @param conditions
	 * @return
	 */
	public Query<T> exclude(Condition... conditions);

	/**
	 * Sets the ordering of the database query.
	 * For descending order, see: .orderBy(DIRECTION, fields)
	 * @param fields
	 * @return
	 */
	public Query<T> orderBy(Field<?>... fields);

	/**
	 * Returns the first n rows of the query.
	 * Same as .limit(n)
	 * @param n
	 * @return
	 */
	public Query<T> top(int n);

	/**
	 * Returns the first n rows of the query.
	 * Same as .top(n)
	 * @param n
	 * @return
	 */
	public Query<T> limit(int n);

	/**
	 * Sets the distinct keyword in the select statement.
	 * @return
	 */
	public Query<T> distinct();

	/**
	 * Joins on foreign keys.  FKed objects are populated under the .getFK() style methods.
	 * Avoids O(n) SQL calls when accessing FKs inside a loop.
	 * @param fields
	 * @return
	 */
	public Query<T> with(Field.FK... fields);

	/**
	 * Don't include the following fields in the select statement.
	 * Note: The returned object will still contain a .getField() method.  If it is called another 
	 * SQL call will be made to fetch this value.  (assuming the PK was not also excluded with this call)
	 * @param fields
	 * @return
	 */
	public Query<T> deferFields(Field<?>... fields);

	/**
	 * Only include the following fields in the select statement.
	 * Note: The returned object will still contain all .getField() methods.  If any are called that were not in this list, another 
	 * SQL call will be made to fetch each value.  (assuming the PK was included with this call)
	 * @param fields
	 * @return
	 */
	public Query<T> onlyFields(Field<?>... fields);

	/**
	 * Returns the last value that would be returned by the query.
	 * Same as: .orderBy(DESCENDING, field).top(1).getTheOnly()
	 * @param field
	 * @return
	 */
	public T latest(Field<?> field);

	/**
	 * Gets the first item in the query.
	 * Same as: .top(1).getTheOnly()
	 * @return
	 */
	public T first();

	/**
	 * Returns whether the SQL returned any rows or not.
	 * Same as: .count() == 0
	 * @return
	 * @throws SQLException
	 */
	public boolean isEmpty() throws SQLException;

	/**
	 * Executes and update statement populated w/ data from .where() and .set().
	 * Example:  SomeClass.ALL.set(SomeClass.SOME_FIELD, "xyz")
	 *                        .where(SomeClass.SOME_FIELD.eq("abc"))
	 *                        .update();
	 * @return
	 * @throws SQLException
	 */
	public int update() throws SQLException;

	/**
	 * Deletes all rows matching data set with: .where()
	 * Example:  SomeClass.ALL.where(SomeClass.SOME_FIELD.eq("abc")).deleteAll()
	 * Use with caution!
	 * @return
	 * @throws SQLException
	 */
	public int deleteAll() throws SQLException;

	/**
	 * Not implemented yet.
	 * @param field
	 * @return
	 */
	public Statistics stats(Field<?>... field);

	/**
	 * Returns an Iterable for this query.  Not usually necessary as the Query itself is Iterable, but useful if you want to 
	 * prevent further filtering or updating for some reason.
	 * @return
	 */
	public Iterable<T> all();

	/**
	 * Returns an always-empty Iterable.  I'm not sure this has any practical use, 
	 * but it seemed to be a good corollary to: .all()
	 * @return
	 */
	public Iterable<T> none();

	/**
	 * Same as .orderBy(fields), but allows you to specify the direction. 
	 * Note: The direction is applied to all the fields.  to specify different
	 * directions to different fields, chain the calls like this:
	 * SomeClass.ALL.orderBy(DESCENDING, SomeClass.SOME_FIELD).orderBy(ASCENDING, SomeClass.SOME_OTHER_FIELD)
	 * @param direction
	 * @param fields
	 * @return
	 */
	public Query<T> orderBy(DIRECTION direction, Field<?>... fields);

	/**
	 * Sets the field to the given value.  Chainable.  Does not execute any SQL until .update() is called.
	 * @param key
	 * @param value
	 * @return
	 */
	public Query<T> set(Field<?> key, Object value);

	/**
	 * Same as calling .set(key, value) for each entry in the Map.
	 * @param values
	 * @return
	 */
	public Query<T> set(Map<Field<?>,Object> values);

	/**
	 * Inserts the values set by .set(key,value).
	 * Note: May be easier to create the object with new SomeClass(), 
	 * then call its setter methods and then .insert() in it.
	 * @return
	 * @throws SQLException
	 */
	public Object insert() throws SQLException;

	/**
	 * Returns the only object returned by this query.  If multiple rows are returned, 
	 * throws a RuntimeException.
	 * @return
	 */
	public T getTheOnly();

	/**
	 * Runs the query, populating a list with all the values returned.
	 * Useful if you need non-linear access to your objects, but take note all 
	 * objects must be able to fit into memory at the same time. 
	 * @return
	 */
	public List<T> asList();

	/**
	 * Same as asList(), but puts them into a HashSet.
	 * @return
	 */
	public Set<T> asSet();

	/**
	 * Sums the value of a field grouped by another field.
	 * Sum is calculated by the database.  Objects are not transferred to the JVM.
	 * @param sumField
	 * @param byField
	 * @return
	 * @throws SQLException
	 */
	public <S> Map<S, Double> sumBy(Field<? extends Number> sumField, Field<S> byField)
			throws SQLException;

	/**
	 * Sums the value of a given field.
	 * Sum is calculated by the database.  Objects are not transferred to the JVM.
	 * @param f
	 * @return
	 * @throws SQLException
	 */
	public Double sum(Field<? extends Number> f) throws SQLException;

	//public <S> Map<S, T> mapBy(Field<S> byField) throws SQLException;

	/**
	 * Counts the rows grouped by a given field.
	 * Sum is calculated by the database.  Objects are not transferred to the JVM.
	 * @param byField
	 * @return
	 * @throws SQLException
	 */
	public <S> Map<S, Integer> countBy(Field<S> byField) throws SQLException;

	/**
	 * Use a given javax.sql.DataSource.
	 * @param ds
	 * @return
	 */
	public Query<T> use(DataSource ds);

}
