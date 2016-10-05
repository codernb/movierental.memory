package ch.fhnw.edu.rental.persistence.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.fhnw.edu.rental.model.Movie;
import ch.fhnw.edu.rental.persistence.MovieRepository;
import ch.fhnw.edu.rental.persistence.PriceCategoryRepository;

@Component
public class MovieRepositoryImpl implements MovieRepository {
	private Map<Long, Movie> data = new HashMap<Long, Movie>();
	private long nextId = 1;

	@Autowired
	private PriceCategoryRepository priceCategoryRepo;
	
	@SuppressWarnings("unused")
	private void initData () {
		data.clear();
		nextId = 1;
		
		Calendar c = Calendar.getInstance();
		c.clear();

		c.set(2016, Calendar.MAY, 11);
		save(new Movie("The Revenant", c.getTime(), priceCategoryRepo.findOne(1L)));

		c.set(2016, Calendar.JULY, 20);
		save(new Movie("Room", c.getTime(), priceCategoryRepo.findOne(1L)));

		c.set(2016, Calendar.AUGUST, 18);
		save(new Movie("Das Dschungelbuch", c.getTime(), priceCategoryRepo.findOne(2L)));

		c.set(2016, Calendar.AUGUST, 31);
		save(new Movie("Eddie the Eagle", c.getTime(), priceCategoryRepo.findOne(3L)));

		c.set(2016, Calendar.SEPTEMBER, 7);
		save(new Movie("The Man Who Knew Infinity", c.getTime(), priceCategoryRepo.findOne(3L)));
	}
	
	@Override
	public Movie findOne(Long id) {
		if(id == null) throw new IllegalArgumentException();
		return data.get(id);
	}

	@Override
	public List<Movie> findAll() {
		return new ArrayList<Movie>(data.values());
	}

	@Override
	public List<Movie> findByTitle(String name) {
		List<Movie> result = new ArrayList<Movie>();
		for(Movie m : data.values()){
			if(m.getTitle().equals(name)) result.add(m);
		}
		return result;
	}

	@Override
	public Movie save(Movie movie) {
		if (movie.getId() == null)
			movie.setId(nextId++);
		data.put(movie.getId(), movie);
		return movie;
	}

	@Override
	public void delete(Movie movie) {
		if(movie == null) throw new IllegalArgumentException();
		data.remove(movie.getId());
		movie.setId(null);
	}

	@Override
	public void delete(Long id) {
		if(id == null) throw new IllegalArgumentException();
		delete(findOne(id));
	}

	@Override
	public boolean exists(Long id) {
		if(id == null) throw new IllegalArgumentException();
		return data.get(id) != null;
	}

	@Override
	public long count() {
		return data.size();
	}

}
