package ch.fhnw.edu.rental.persistence.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import ch.fhnw.edu.rental.model.User;
import ch.fhnw.edu.rental.persistence.RentalRepository;
import ch.fhnw.edu.rental.persistence.UserRepository;

@Primary
@Repository
@ConfigurationProperties(prefix = "movierental.users")
public class UserRepositoryJdbc extends AbstractRepository<User> implements UserRepository {

	@Autowired
	private RentalRepository rentalRepository;

	public UserRepositoryJdbc() {
		super(User::getId, (u, i) -> u.setId(i));
	}

	@Override
	public List<User> findByLastName(String lastName) {
		List<User> referenced = referenceHolder.values().stream().filter(u -> u.getLastName().equals(lastName))
				.collect(Collectors.toList());
		List<Long> referencedIds = referenced.stream().map(u -> u.getId()).collect(Collectors.toList());
		String query = String.format("SELECT * FROM %s WHERE USER_NAME = ?", table);
		List<User> loaded = jdbcTemplate.query(query, this::createInstance, lastName);
		loaded.stream().filter(u -> !referencedIds.contains(u.getId())).forEach(u -> {
			referenced.add(u);
			referenceHolder.put(u.getId(), u);
		});
		return referenced;
	}

	@Override
	public List<User> findByFirstName(String firstName) {
		List<User> referenced = referenceHolder.values().stream().filter(u -> u.getLastName().equals(firstName))
				.collect(Collectors.toList());
		List<Long> referencedIds = referenced.stream().map(u -> u.getId()).collect(Collectors.toList());
		String query = String.format("SELECT * FROM %s WHERE USER_FIRSTNAME = ?", table);
		List<User> loaded = jdbcTemplate.query(query, this::createInstance, firstName);
		loaded.stream().filter(u -> !referencedIds.contains(u.getId())).forEach(u -> {
			referenced.add(u);
			referenceHolder.put(u.getId(), u);
		});
		return referenced;
	}

	@Override
	public List<User> findByEmail(String email) {
		List<User> referenced = referenceHolder.values().stream().filter(u -> u.getLastName().equals(email))
				.collect(Collectors.toList());
		List<Long> referencedIds = referenced.stream().map(u -> u.getId()).collect(Collectors.toList());
		String query = String.format("SELECT * FROM %s WHERE USER_EMAIL = ?", table);
		List<User> loaded = jdbcTemplate.query(query, this::createInstance, email);
		loaded.stream().filter(u -> !referencedIds.contains(u.getId())).forEach(u -> {
			referenced.add(u);
			referenceHolder.put(u.getId(), u);
		});
		return referenced;
	}

	@Override
	protected Map<String, Function<User, ?>> getColumns() {
		Map<String, Function<User, ?>> columns = new HashMap<>();
		columns.put("USER_ID", u -> u.getId());
		columns.put("USER_EMAIL", u -> u.getEmail());
		columns.put("USER_FIRSTNAME", u -> u.getFirstName());
		columns.put("USER_NAME", u -> u.getLastName());
		return columns;
	}

	@Override
	protected User createInstance(ResultSet resultSet, int row) throws SQLException {
		User user = new User(resultSet.getString("USER_NAME"), resultSet.getString("USER_FIRSTNAME"));
		user.setId(resultSet.getLong("USER_ID"));
		user.setEmail(resultSet.getString("USER_EMAIL"));
		referenceHolder.put(user.getId(), user);
		user.setRentals(rentalRepository.findByUser(user));
		return user;
	}

}
