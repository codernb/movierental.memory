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
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import ch.fhnw.edu.rental.model.Movie;
import ch.fhnw.edu.rental.persistence.MovieRepository;
import ch.fhnw.edu.rental.persistence.PriceCategoryRepository;

@Primary
@Repository
@ConfigurationProperties(prefix = "movierental.movies")
public class MovieRepositoryJdbc extends AbstractRepository<Movie> implements MovieRepository {

	@Autowired
	private PriceCategoryRepository priceCategoryRepository;

	public MovieRepositoryJdbc() {
		super(Movie::getId, (m, i) -> m.setId(i));
	}
	
	@Override
	public List<Movie> findByTitle(String title) {
		List<Movie> referenced = referenceHolder.values().stream().filter(m -> m.getTitle().equals(title))
				.collect(Collectors.toList());
		List<Long> referencedIds = referenced.stream().map(m -> m.getId()).collect(Collectors.toList());
		String query = "SELECT * FROM MOVIES WHERE MOVIE_TITLE = ?";
		List<Movie> loaded = jdbcTemplate.query(query, this::createInstance, title);
		loaded.stream().filter(m -> !referencedIds.contains(m.getId())).forEach(m -> {
			referenced.add(m);
			referenceHolder.putIfAbsent(m.getId(), m);
		});
		return referenced;
	}

	@Override
	protected Map<String, Function<Movie, ?>> getColumns() {
		Map<String, Function<Movie, ?>> columns = new HashMap<>();
		columns.put("MOVIE_RELEASEDATE", m -> new Date(m.getReleaseDate().getTime()));
		columns.put("MOVIE_TITLE", m -> m.getTitle());
		columns.put("MOVIE_RENTED", m -> m.isRented());
		columns.put("PRICECATEGORY_FK", m -> m.getPriceCategory().getId());
		return columns;
	}

	@Override
	protected Movie createInstance(ResultSet resultSet, int row) throws SQLException {
		Movie movie = new Movie(resultSet.getString("MOVIE_TITLE"), resultSet.getDate("MOVIE_RELEASEDATE"),
				priceCategoryRepository.findOne(resultSet.getLong("PRICECATEGORY_FK")));
		movie.setId(resultSet.getLong("MOVIE_ID"));
		movie.setRented(resultSet.getBoolean("MOVIE_RENTED"));
		referenceHolder.putIfAbsent(movie.getId(), movie);
		return movie;
	}

}
