package com.example.service;

import com.example.entities.News;

public interface NewsService {
	public News findById(Integer id);
	
	public void saveOrEdit(News news);
}
