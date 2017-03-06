package com.example.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import com.example.entities.News;
import com.example.service.config.ServiceContextTest;

public class NewsServiceTest extends ServiceContextTest {
	
	
	
	@Test
	@Transactional
	@Commit
	public void aCreate(){
		News newsExpected = new News(1, "karlo", "super new");
		newsService.saveOrEdit(newsExpected);
		
		News newsActual = newsService.findById(1);
		
		assertThat("the news are different, should be the same", 
				newsExpected.getNews(), equalTo(newsActual.getNews()));
	}
}

