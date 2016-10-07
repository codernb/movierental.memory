package ch.fhnw.edu.rental.persistence.jdbc;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class AbstractRepository<T> {

	@NotEmpty
	protected String table;

	@NotEmpty
	protected String idFieldName;

	@Autowired
	protected DataSource dataSource;

	@Autowired
	protected JdbcTemplate jdbcTemplate;

	protected final HashMap<Long, T> referenceHolder = new HashMap<>();

	private final Function<T, Long> idFunction;
	private final BiConsumer<T, Long> setIdMethod;

	private String selectQuery;
	private String selectWhereIdQuery;
	private String deleteQuery;
	private String existsQuery;
	private String countQuery;
	private String insertQuery;
	private String updateQuery;

	public AbstractRepository(Function<T, Long> getIdMethod, BiConsumer<T, Long> setIdMethod) {
		this.idFunction = getIdMethod;
		this.setIdMethod = setIdMethod;
	}

	@PostConstruct
	private void postConstruct() {
		selectQuery = String.format("SELECT * FROM %s", table);
		selectWhereIdQuery = String.format("%s WHERE %s = ?", selectQuery, idFieldName);
		deleteQuery = String.format("DELETE FROM %s WHERE %s = ?", table, idFieldName);
		existsQuery = MessageFormat.format(
				"SELECT DISTINCT COUNT(*) FROM {0} WHERE EXISTS (SELECT * FROM {0} WHERE {1} = ?)", table, idFieldName);
		countQuery = String.format("SELECT DISTINCT COUNT(*) FROM {0} WHERE EXISTS (SELECT * FROM %s)", table);
		insertQuery = String.format("INSERT INTO %s(%s) VALUES(%s)", table, getInsertFields(), getInsertPlaceholders());
		updateQuery = String.format("UPDATE %s SET %s WHERE %s = ?", table, getUpdatePlaceholders(), idFieldName);

	}

	public T findOne(Long id) {
		return referenceHolder.values().stream().filter(t -> id.equals(idFunction.apply(t))).findAny()
				.orElseGet(() -> jdbcTemplate.queryForObject(selectWhereIdQuery, this::createInstance, id));
	}

	public List<T> findAll() {
		List<T> loaded = jdbcTemplate.query(selectQuery, this::createInstance);
		loaded.forEach(t -> referenceHolder.putIfAbsent(idFunction.apply(t), t));
		List<Long> missmachedIds = referenceHolder.keySet().stream()
				.filter(i -> !loaded.stream().map(idFunction).collect(Collectors.toList()).contains(i))
				.collect(Collectors.toList());
		missmachedIds.forEach(referenceHolder::remove);
		return new ArrayList<>(referenceHolder.values());
	}

	public T save(T t) {
		if (idFunction.apply(t) == null)
			return create(t);
		else
			return update(t);
	}

	public void delete(Long id) {
		delete(findOne((Long) id));
	}

	public void delete(T t) {
		jdbcTemplate.update(deleteQuery, idFunction.apply(t));
		setIdMethod.accept(t, null);
		referenceHolder.remove(idFunction.apply(t));
	}

	public boolean exists(Long id) {
		return jdbcTemplate.update(existsQuery, id) > 0;
	}

	public long count() {
		return jdbcTemplate.update(countQuery);
	}

	protected T create(T t) {
		referenceHolder.put(idFunction.apply(t), t);
		try (Connection connection = dataSource.getConnection()) {
			PreparedStatement statement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
			int i = 1;
			for (Function<T, ?> valueSupplier : getColumns().values()) {
				Object value = valueSupplier.apply(t);
				if (value == null)
					statement.setNull(i, Types.NULL);
				else if (value instanceof Date)
					statement.setDate(i, (Date) value);
				else if (value instanceof String)
					statement.setString(i, (String) value);
				else if (value instanceof Boolean)
					statement.setBoolean(i, (Boolean) value);
				else if (value instanceof Long)
					statement.setLong(i, (Long) value);
				else if (value instanceof Integer)
					statement.setInt(i, (Integer) value);
				else
					throw new RuntimeException("Type not recognized: " + value);
				i++;
			}
			statement.executeUpdate();
			ResultSet resultSet = statement.getGeneratedKeys();
			resultSet.next();
			setIdMethod.accept(t, resultSet.getLong(1));
			return t;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	protected T update(T t) {
		// XXX a bug?: jdbcTemplate.update(updateQuery,
		// buggy_getUpdateArguments(t), idFunction.apply(t));
		jdbcTemplate.update(updateQuery, getUpdateArguments(t));
		return t;
	}

	protected abstract Map<String, Function<T, ?>> getColumns();

	protected abstract T createInstance(ResultSet resultSet, int row) throws SQLException;

	private String getInsertFields() {
		return String.join(", ", getColumns().keySet());
	}

	private String getInsertPlaceholders() {
		String[] placeholders = new String[getColumns().size()];
		Arrays.fill(placeholders, "?");
		return String.join(", ", placeholders);
	}

	private String getUpdatePlaceholders() {
		return String.join(", ", getColumns().keySet().stream().sorted().map(p -> String.format("%s = ?", p))
				.collect(Collectors.toList()));
	}

	private Object[] getUpdateArguments(T t) {
		Map<String, Function<T, ?>> columns = getColumns();
		Object[] arguments = new Object[columns.size() + 1];
		int i = 0;
		for (String columnName : columns.keySet().stream().sorted().collect(Collectors.toList()))
			arguments[i++] = columns.get(columnName).apply(t);
		arguments[i] = idFunction.apply(t);
		return arguments;
	}

	// // XXX: the buggy funciton
	// private Object[] buggy_getUpdateArguments(T t) {
	// Map<String, Function<T, ?>> columns = getColumns();
	//
	// // XXXObject[] arguments = new Object[columns.size() + 1];
	// Object[] arguments = new Object[columns.size()];
	//
	// int i = 0;
	// for (String columnName :
	// columns.keySet().stream().sorted().collect(Collectors.toList()))
	// arguments[i++] = columns.get(columnName).apply(t);
	//
	// // XXX: arguments[i] = idFunction.apply(t);
	//
	// return arguments;
	// }

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getIdFieldName() {
		return idFieldName;
	}

	public void setIdFieldName(String idFieldName) {
		this.idFieldName = idFieldName;
	}

}
