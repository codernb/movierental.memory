package ch.fhnw.edu.rental.persistence.jdbc;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import ch.fhnw.edu.rental.model.Movie;
import ch.fhnw.edu.rental.persistence.MovieRepository;
import ch.fhnw.edu.rental.persistence.PriceCategoryRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "gui=false" })
@Transactional
public class MovieRepositoryJdbcTest {

	@Autowired
	private MovieRepository movieRepository;
	
	@Autowired
	private PriceCategoryRepository priceCategoryRepository;
	
	@Test
	public void testFindOne() throws Exception {
		System.err.println(movieRepository.findAll().size());
		movieRepository.save(new Movie("test", new Date(), priceCategoryRepository.findOne(1L)));
	}
	
	@Test
	public void testFindOne1() throws Exception {
		System.err.println(movieRepository.findAll().size());
	}

}
