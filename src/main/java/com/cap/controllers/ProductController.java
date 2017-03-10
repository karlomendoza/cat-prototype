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

import com.cap.models.Product;
import com.cap.models.ProductRepository;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RestController
public class ProductController {

	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private JedisPool jedisPool;

	
	@Transactional
	@GetMapping
	@RequestMapping("/avail/{id}")
	@CrossOrigin
	public Integer getProductAvail(@PathVariable("id") int id) {
		
		String response;
		try(Jedis jedis = jedisPool.getResource()){
			response = jedis.get("product_avail_" + id);
		}
		
		return Integer.parseInt(response);
	}
	
	@Transactional
	@GetMapping
	@RequestMapping("/product/{id}")
	@CrossOrigin
	public String getProduct(@PathVariable("id") int id) {
		
		String product;
		try(Jedis jedis = jedisPool.getResource()){
			product = jedis.get("product_" + id);
		}
		
		return product;
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