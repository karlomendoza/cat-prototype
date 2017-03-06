package com.example.service.config;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.example.service.NewsService;

@Ignore("ignored so tests step does not try to run tests in here and fail")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceContext.class)
public class ServiceContextTest {
	
	@Autowired
	protected NewsService newsService;

}

