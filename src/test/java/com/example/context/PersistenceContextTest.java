package com.example.context;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.example.persistence.config.PersistenceJPAConfig;
import com.example.repository.NewsRepository;

@Ignore("ignore this so mvn tests does not try to run this class")
@RunWith(SpringJUnit4ClassRunner.class)	
@ContextConfiguration(classes=PersistenceJPAConfig.class)
public class PersistenceContextTest {
	
	@Autowired
	protected NewsRepository newsRepository;
}
