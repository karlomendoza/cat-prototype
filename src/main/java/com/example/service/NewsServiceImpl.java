package com.example.service;


import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.entities.News;
import com.example.repository.NewsRepository;

@Service("newsService")
public class NewsServiceImpl implements NewsService {

	@Autowired
	private NewsRepository newsRepository;

		
	@Override
	@Transactional
	public News findById(Integer id) {
		
		if(id == null || id == 0){
			return null;
		}
		News news;
		try{
			news = newsRepository.getOne(id);
			news.getId();
		} catch(EntityNotFoundException ex){
			news = new News();
			//no entity found return empty object
		}
		return news;
	}
	
	@Override
	public void saveOrEdit(News news) {
		
		//TODO add some validations later maybe
		newsRepository.save(news);
		
	}
	
	

}
