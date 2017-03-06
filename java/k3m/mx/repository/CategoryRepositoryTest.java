package k3m.mx.repository;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import k3m.mx.context.PersistenceContextTest;
import k3m.mx.entities.Category;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CategoryRepositoryTest extends PersistenceContextTest {

	@Test
	@Transactional
	@Commit
	public void aCreate(){
		Category categoryExpected = new Category(1, "misterio", "cuentos de misterio uuuuu que miedo");
		categoryRepository.saveAndFlush(categoryExpected);
		
		Category categoryActual = categoryRepository.getOne(1);
		
		assertThat("the books names are different, should be the same", 
				categoryExpected.getName(), equalTo(categoryActual.getName()));
	}
	
	@Test
	public void count() {

		long numberCategory = categoryRepository.count();
		assertThat("There should atleast exist one category", numberCategory, greaterThan(0L));
	}

	@Test
	public void findAllCategories() {
		List<Category> categorys = categoryRepository.findAll();
		assertThat("this should not be empty", categorys, is(not(empty())));
	}
	
	@Transactional
	@Commit
	@Test(expected=EntityNotFoundException.class)
	public void zDelete(){
		Category category = categoryRepository.getOne(1);
		categoryRepository.delete(category);
		categoryRepository.flush();
		Category categoryActual = categoryRepository.getOne(1);
		categoryActual.getIdCategory();
		
	}

}