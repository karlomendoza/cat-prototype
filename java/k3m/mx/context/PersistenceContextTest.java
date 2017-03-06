package k3m.mx.context;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import k3m.mx.config.PersistenceJPAConfig;
import k3m.mx.repository.AuthorRepository;
import k3m.mx.repository.BookRepository;
import k3m.mx.repository.CategoryRepository;
import k3m.mx.repository.StudentRepository;

@Ignore("ignore this so mvn tests does not try to run this class")
@RunWith(SpringJUnit4ClassRunner.class)	
@ContextConfiguration(classes=PersistenceJPAConfig.class)
public class PersistenceContextTest {
	
	@Autowired
	protected BookRepository bookRepository;
	
	@Autowired
	protected AuthorRepository authorRepository;
	
	@Autowired
	protected CategoryRepository categoryRepository;
	
	@Autowired
	protected StudentRepository studentRepository;
	
	
}
