package k3m.mx.repository;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import k3m.mx.context.PersistenceContextTest;
import k3m.mx.entities.Author;
import k3m.mx.entities.Book;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BookRepositoryTest extends PersistenceContextTest {

	@Test
	@Transactional
	@Commit
	public void aCreate(){
		Book bookExpected = new Book("ccc", "lo que el viento se dejo", new Date());
		Author author = new Author(null, "karlo", "mendoza");
		
		bookExpected.setAuthor(author);
		
		bookRepository.save(bookExpected);
		bookRepository.flush();
		
		Book bookActual = bookRepository.getOne("ccc");
		
		assertThat("the books names are different, should be the same", 
				bookExpected.getTitle(), equalTo(bookActual.getTitle()));
		
		
	}

	@Test
	public void count() {

		long numberBooks = bookRepository.count();
		assertThat("there should be atleast one book", numberBooks,  greaterThan(0L));
	}

	@Test
	public void findAllBooks() {
		List<Book> books = bookRepository.findAll();
		assertThat("this should not be empty", books, is(not(empty())));
	}

	@Test
	public void findByTitle() {
		String titleExpected = "java";
		List<Book> books = bookRepository.findByTitle(titleExpected);
		String titleActual = books.get(0).getTitle();
		assertThat("titles should be the same", titleActual, equalTo(titleExpected));
	}
	
	@Test
	@Transactional
	public void bookHasAuthor(){
		Book book = bookRepository.getOne("ccc");
		assertThat("a book should have an author", book.getAuthor(), notNullValue());
	}
}