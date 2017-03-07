package cap.controllers;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cap.models.News;
import cap.models.NewsRepository;

@RestController
@RequestMapping("/news")
public class NewsController {

	@Autowired
	private NewsRepository newsRepository;
	
	@GetMapping
	@RequestMapping
	public List<News> getNews() {
		return newsRepository.findAll();
	}
	
	@Transactional
	@GetMapping
	@RequestMapping("/find/{id}")
	public News getNew(@PathVariable("id") int id) {
		News news = newsRepository.getOne(id);
		news.getAuthor();
		return news;
	}
	
	@Transactional
	@GetMapping
	@RequestMapping("/create/{author}/{news}")
	public News create(@PathVariable("author") String author, 
						@PathVariable("news") String news ) {
		
		return newsRepository.saveAndFlush(new News(author, news));
	}

}