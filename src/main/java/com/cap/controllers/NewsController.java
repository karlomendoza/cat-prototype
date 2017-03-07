package com.cap.controllers;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cap.models.News;
import com.cap.models.NewsRepository;

import redis.clients.jedis.Jedis;

@RestController
@RequestMapping("/news")
public class NewsController {

	@Autowired
	private NewsRepository newsRepository;
	
	@GetMapping
	@RequestMapping
	@CrossOrigin
	public List<News> getNews() {
		return newsRepository.findAll();
	}
	
	@GetMapping
	@RequestMapping("/getAllCached")
	@CrossOrigin
	public List<String> getNewsCached() {
		Jedis jedis = new Jedis();
		
		List<News> allNews;
		List<String> response = jedis.lrange("allNews", 0, -1);
		if(response == null || response.isEmpty()){
			allNews = newsRepository.findAll();
			allNews.forEach(news -> jedis.lpush("allNews", news.toString()));
			response = jedis.lrange("allNews", 0, -1);
		}
		jedis.close();
		
		return response;
	}
	
	@Transactional
	@GetMapping
	@RequestMapping("/find/{id}")
	@CrossOrigin
	public News getNew(@PathVariable("id") int id) {
		News news = newsRepository.getOne(id);
		news.getAuthor();
		return news;
	}
	
	@Transactional
	@PostMapping
	@CrossOrigin
	@RequestMapping("/create/{author}/{news}")
	public News create(@PathVariable("author") String author, 
						@PathVariable("news") String news ) {
		
		Jedis jedis = new Jedis();
		
		News newNews = newsRepository.saveAndFlush(new News(author, news));
		
		List<String> response = jedis.lrange("allNews", 0, 1);
		if(response != null && !response.isEmpty()){
			jedis.lpush("allNews", newNews.toString());
		}
		jedis.close();

		return newNews;
	}

}