package ch.fhnw.edu.rental.persistence.jdbc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import ch.fhnw.edu.rental.persistence.RentalRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "gui=false" })
@Transactional
public class RentalRepositoryJdbcTest {
	
	@Autowired
	private RentalRepository rentalRepository;

	@Test
	public void testFindOne() throws Exception {
		
		System.err.println(rentalRepository.findOne(1L));
		
	}

}
