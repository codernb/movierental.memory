package ch.fhnw.edu.rental.persistence.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import ch.fhnw.edu.rental.model.PriceCategory;
import ch.fhnw.edu.rental.model.PriceCategoryChildren;
import ch.fhnw.edu.rental.model.PriceCategoryNewRelease;
import ch.fhnw.edu.rental.model.PriceCategoryRegular;
import ch.fhnw.edu.rental.persistence.PriceCategoryRepository;

@Primary
@Repository
@ConfigurationProperties(prefix = "movierental.pricecategories")
public class PriceCategoryRepositoryJdbc extends AbstractRepository<PriceCategory> implements PriceCategoryRepository {

	public PriceCategoryRepositoryJdbc() {
		super(PriceCategory::getId, (p, i) -> {
			throw new UnsupportedOperationException();
		});
	}

	@Override
	protected Map<String, Function<PriceCategory, ?>> getColumns() {
		Map<String, Function<PriceCategory, ?>> columns = new HashMap<>();
		columns.put("PRICECATEGORY_TYPE", m -> {
			throw new UnsupportedOperationException();
		});
		return columns;
	}

	@Override
	protected PriceCategory createInstance(ResultSet resultSet, int row) throws SQLException {
		PriceCategory priceCategory = null;
		switch (resultSet.getString("PRICECATEGORY_TYPE")) {
		case "Regular":
			priceCategory = new PriceCategoryRegular();
			break;

		case "Children":
			priceCategory = new PriceCategoryChildren();
			break;

		case "NewRelease":
			priceCategory = new PriceCategoryNewRelease();
			break;

		}
		if (priceCategory == null)
			throw new RuntimeException("No such price category.");
		priceCategory.setId(resultSet.getLong(idFieldName));
		referenceHolder.put(priceCategory.getId(), priceCategory);
		return priceCategory;
	}

}
