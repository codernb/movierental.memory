package ch.fhnw.edu.rental.persistence.jdbc;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import ch.fhnw.edu.rental.model.Movie;
import ch.fhnw.edu.rental.model.PriceCategoryChildren;
import ch.fhnw.edu.rental.model.Rental;
import ch.fhnw.edu.rental.model.User;
import ch.fhnw.edu.rental.persistence.MovieRepository;
import ch.fhnw.edu.rental.persistence.RentalRepository;
import ch.fhnw.edu.rental.persistence.UserRepository;

@Primary
@Repository
@ConfigurationProperties(prefix = "movierental.rentals")
public class RentalRepositoryJdbc extends AbstractRepository<Rental> implements RentalRepository {

	@Lazy
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MovieRepository movieRepository;

	public RentalRepositoryJdbc() {
		super(Rental::getId, (r, i) -> r.setId(i));
	}

	@Override
	public List<Rental> findByUser(User user) {
		List<Rental> referenced = referenceHolder.values().stream().filter(r -> r.getUser().equals(user))
				.collect(Collectors.toList());
		List<Long> referencedIds = referenced.stream().map(r -> r.getId()).collect(Collectors.toList());
		String query = String.format("SELECT * FROM %s WHERE USER_ID = ?", table);
		List<Rental> loaded = jdbcTemplate.query(query, this::createInstance, user.getId());
		loaded.stream().filter(r -> !referencedIds.contains(r.getId())).forEach(r -> {
			referenced.add(r);
			referenceHolder.put(r.getId(), r);
		});
		return referenced;
	}

	@Override
	protected Map<String, Function<Rental, ?>> getColumns() {
		Map<String, Function<Rental, ?>> columns = new HashMap<>();
		columns.put("RENTAL_ID", m -> m.getId());
		columns.put("RENTAL_RENTALDATE", m -> new Date(m.getRentalDate().getTime()));
		columns.put("RENTAL_RENTALDAYS", m -> m.getRentalDays());
		columns.put("USER_ID", m -> m.getUser().getId());
		columns.put("MOVIE_ID", m -> m.getMovie().getId());
		return columns;
	}

	@Override
	protected Rental createInstance(ResultSet resultSet, int row) throws SQLException {
		Rental rental = new Rental(new User("", ""),
				new Movie("", new Date(0), new PriceCategoryChildren()), resultSet.getInt("RENTAL_RENTALDAYS"),
				resultSet.getDate("RENTAL_RENTALDATE"));
		referenceHolder.put(rental.getId(), rental);
		rental.setUser(userRepository.findOne(resultSet.getLong("USER_ID")));
		rental.setMovie(movieRepository.findOne(resultSet.getLong("MOVIE_ID")));
		rental.setId(resultSet.getLong("RENTAL_ID"));
		return rental;
	}

}
