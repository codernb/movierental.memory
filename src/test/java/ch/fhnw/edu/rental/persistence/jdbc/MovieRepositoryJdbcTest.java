package ch.fhnw.edu.rental.persistence.jdbc;

import java.util.Date;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import ch.fhnw.edu.rental.model.Movie;
import ch.fhnw.edu.rental.persistence.MovieRepository;
import ch.fhnw.edu.rental.persistence.PriceCategoryRepository;
import ch.fhnw.edu.rental.persistence.RentalRepository;
import ch.fhnw.edu.rental.persistence.UserRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "gui=false" })
@Transactional
public class MovieRepositoryJdbcTest {

	@Autowired
	private MovieRepository movieRepository;
	
	@Autowired
	private RentalRepository rentalRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PriceCategoryRepository priceCategoryRepository;
	
	@After
	public void tearDown() 
	{
		rentalRepository.findAll().forEach(rentalRepository::delete);
		movieRepository.findAll().forEach(movieRepository::delete);
		userRepository.findAll().forEach(userRepository::delete);
		System.err.println(movieRepository.count());
	}
	
	@Test
	public void testFindOne() throws Exception {
		System.err.println(movieRepository.count());
		movieRepository.save(new Movie("test", new Date(), priceCategoryRepository.findOne(1L)));
	}
	
	@Test
	public void testFindOne1() throws Exception {
		System.err.println(movieRepository.count());
	}

}
