package com.example.repository;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.transaction.annotation.Transactional;

import com.example.context.PersistenceContextTest;
import com.example.entities.News;


public class NewsRepositoryTest extends PersistenceContextTest {

	@Test
	@Transactional
	public void aCreate(){
		News newsExpected = new News(1, "karlo", "super new");
		newsRepository.saveAndFlush(newsExpected);
		
		News newsActual = newsRepository.getOne(1);
		
		assertThat("the news are different, should be the same", 
				newsExpected.getNews(), equalTo(newsActual.getNews()));
	}
	
}