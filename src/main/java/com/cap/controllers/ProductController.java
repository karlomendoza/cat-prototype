package com.cap.controllers;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cap.models.Product;
import com.cap.models.ProductRepository;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RestController
public class ProductController {

	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private JedisPool jedisPool;

	
	@Transactional
	@CrossOrigin
	@RequestMapping(value = "/avail/{id}", method = RequestMethod.GET)
	@ApiOperation(value = "Check the availability of a specific product")
	public Integer getProductAvail(@ApiParam(value = "Product ID") @PathVariable("id") int id) {
		
		String response;
		try(Jedis jedis = jedisPool.getResource()){
			response = jedis.get("product_avail_" + id);
		}
		
		return Integer.parseInt(response);
	}
	
	@Transactional
	@CrossOrigin
	@RequestMapping(value = "/product/{id}", method = RequestMethod.GET)
	@ApiOperation(value = "Get a product")
	public String getProduct(@ApiParam(value = "Product ID") @PathVariable("id") int id) {
		
		String product;
		try(Jedis jedis = jedisPool.getResource()){
			product = jedis.get("product_" + id);
		}
		
		return product;
	}
	
	@Transactional
	@CrossOrigin
	@RequestMapping(value = "/create/{description}/{inventory}/{price}", method = RequestMethod.POST)
	@ApiOperation(value = "Create a product")
	public Product create(@ApiParam(value = "Description") @PathVariable("description") String description, 
						@ApiParam(value = "How many products are there?") @PathVariable("inventory") Integer inventory,
						@ApiParam(value = "Price") @PathVariable("price") Double price) {

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