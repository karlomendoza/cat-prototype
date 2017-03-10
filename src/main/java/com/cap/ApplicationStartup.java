package com.cap;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.cap.models.Order;
import com.cap.models.OrderRepository;
import com.cap.models.Product;
import com.cap.models.ProductRepository;
import com.cap.util.Util;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

	@Autowired
	private JedisPool jedisPool;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private OrderRepository orderRepository;
	
	/**
	 * This event is executed as late as conceivably possible to indicate that
	 * the application is ready to service requests.
	 * 
	 * Its in charge of loading the data from database to the redis service
	 */
	@Override
	public void onApplicationEvent(final ApplicationReadyEvent event) {

		init();
		return;
	}
	
	public void init(){
		ObjectMapper mapper = new ObjectMapper();
		try(Jedis jedis = jedisPool.getResource()){
			List<Product> products = productRepository.findAll();
			String response;
			for (Product product : products) {
				response = jedis.get("product_" + product.getId());
				if(response == null || response.isEmpty()){
					jedis.set("product_" + product.getId(), mapper.writeValueAsString(product));
				}
				
				response = jedis.get("product_avail_" + product.getId());
				if(response == null || response.isEmpty()){
					jedis.set("product_avail_" + product.getId(), product.getInventory().toString());
				}
			}
			
			
			List<Order> orders = orderRepository.findAll();
			List<String> allOrders = jedis.lrange("allOrders", 0, 1);
			if(allOrders == null || allOrders.isEmpty()){
				Util.avoidCicles(orders);
				for (Order order : orders) {
					jedis.lpush("allOrders", mapper.writeValueAsString(order));
				}
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

}