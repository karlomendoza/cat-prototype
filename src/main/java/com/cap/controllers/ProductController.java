package com.cap.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cap.models.Product;
import com.cap.models.ProductRepository;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

@RestController
@RequestMapping("/avail")
public class ProductController {

	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private JedisPool jedisPool;
	
//	@GetMapping
//	@RequestMapping("/getAllCached")
//	@CrossOrigin
//	public List<String> getProductsCached() {
//		
//		List<Product> allProducts;
//		List<String> response = new ArrayList<>();
//		try(Jedis jedis = jedisPool.getResource()){
//			response = jedis.lrange("allProducts", 0, -1);
//			if(response == null || response.isEmpty()){
//				allProducts = productRepository.findAll();
//				allProducts.forEach(product -> jedis.lpush("allProducts", product.toString()));
//				response = jedis.lrange("allProducts", 0, -1);
//			}
//		} catch (JedisConnectionException ex){
//			allProducts = productRepository.findAll();
//			for (Product product: allProducts) {
//				response.add(product.toString());
//			}
//		}
//		return response;
//	}
	
	@Transactional
	@GetMapping
	@RequestMapping("/{id}")
	@CrossOrigin
	public Integer getProducts(@PathVariable("id") int id) {
		Product product = productRepository.getOne(id);
		return product.getInventory();
	}
	
	@Transactional
	@PostMapping
	@CrossOrigin
	@RequestMapping("/create/{description}/{inventory}/{price}")
	public Product create(@PathVariable("description") String description, 
						@PathVariable("inventory") Integer inventory,
						@PathVariable("price") Double price) {

		Product newProduct = productRepository.saveAndFlush(new Product(description, inventory, price));
		try(Jedis jedis = jedisPool.getResource()){
			List<String> response = jedis.lrange("allProducts", 0, 1);
			if(response != null && !response.isEmpty()){
				jedis.lpush("allProducts", newProduct.toString());
			}
		}
		return newProduct;
	}

}