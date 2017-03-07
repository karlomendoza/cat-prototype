package cap.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cap.models.News;
import cap.models.NewsRepository;

@RestController
public class NewsController {

	@Autowired
	private NewsRepository newsRepository;
	
	@GetMapping
	@RequestMapping("/news")
	public List<News> getNews() {
		return newsRepository.findAll();
		
	}

}