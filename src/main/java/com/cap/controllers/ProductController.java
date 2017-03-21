package com.cap.controllers;

import java.io.IOException;
import java.util.ArrayList;
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
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	
	private static ObjectMapper mapper = new ObjectMapper();
	
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
	@RequestMapping(value = "/products", method = RequestMethod.GET)
	@ApiOperation(value = "Get all the products")
	public List<Product> getProducts() throws JsonParseException, JsonMappingException, IOException {
		
		List<String> allProducts;
		try(Jedis jedis = jedisPool.getResource()){
			allProducts = jedis.lrange("allProducts", 0, -1);
		}
		List<Product> products = new ArrayList<>();
		for (String product : allProducts) {
			products.add(mapper.readValue(product, Product.class));
		}
		
		return products;
	}
	
	@Transactional
	@CrossOrigin
	@RequestMapping(value = "/create/{description}/{inventory}/{price}", method = RequestMethod.POST)
	@ApiOperation(value = "Create a product")
	public Product create(@ApiParam(value = "Description") @PathVariable("description") String description, 
						@ApiParam(value = "How many products are there?") @PathVariable("inventory") Integer inventory,
						@ApiParam(value = "Price") @PathVariable("price") Double price) throws JsonProcessingException {

		Product newProduct = productRepository.saveAndFlush(new Product(description, inventory, price));
		try(Jedis jedis = jedisPool.getResource()){
			List<String> response = jedis.lrange("allProducts", 0, 1);
			if(response != null && !response.isEmpty()){				
				jedis.set("product_" + newProduct.getId(), mapper.writeValueAsString(newProduct));
				jedis.set("product_avail_" + newProduct.getId(), newProduct.getInventory().toString());
				jedis.lpush("allProducts", mapper.writeValueAsString(newProduct));
			}
		}
		return newProduct;
	}

}