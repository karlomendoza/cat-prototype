package com.cap.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cap.models.Order;
import com.cap.models.OrderProduct;
import com.cap.models.OrderRepository;
import com.cap.models.Product;
import com.cap.models.ProductRepository;
import com.cap.models.SaleOrder;
import com.cap.util.Util;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RestController
@RequestMapping
public class OrderController {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private JedisPool jedisPool;
	
	private static ObjectMapper mapper = new ObjectMapper();

	@GetMapping
	@RequestMapping("/orderList")
	@CrossOrigin
	public List<Order> getOrders() throws JsonParseException, JsonMappingException, IOException {
		List<String> allOrders = new ArrayList<>();
		List<Order> orders = new ArrayList<>();
		
		try (Jedis jedis = jedisPool.getResource()) {
			allOrders = jedis.lrange("allOrders", 0, -1);
		}
		for (String order: allOrders) {
			orders.add(mapper.readValue(order, Order.class));
		}
		
		return orders;
	}

	@Transactional
	@CrossOrigin
	@RequestMapping(method = RequestMethod.POST, value = "/order")
	public boolean create(@RequestBody SaleOrder saleOrder) throws JsonProcessingException {

		// check availability
		try (Jedis jedis = jedisPool.getResource()) {
			for (OrderProduct orderProduct : saleOrder.getProducts()) {
				Integer availability = Integer.parseInt(jedis.get("product_avail_" + orderProduct.getIdProduct()));
				if (availability < orderProduct.getQuantity()) {
					return false;
				}
			}
		}

		Order order = new Order(saleOrder.getOrderNumber(), new Date(), saleOrder.getCustomerName(),
				saleOrder.getTotal());

		order.setOrderProducts(new ArrayList<>());
		for (OrderProduct orderProduct : saleOrder.getProducts()) {
			OrderProduct op = new OrderProduct();
			op.setOrder(order);
			op.setIdProduct(orderProduct.getIdProduct());
			op.setQuantity(orderProduct.getQuantity());
			op.setPrice(orderProduct.getPrice());
			order.getOrderProducts().add(op);
		}

		orderRepository.saveAndFlush(order);
		
		try (Jedis jedis = jedisPool.getResource()) {
			for (OrderProduct orderProduct : saleOrder.getProducts()) {
				Integer availability = Integer.parseInt(jedis.get("product_avail_" + orderProduct.getIdProduct()));
				Integer newAvailability = availability - orderProduct.getQuantity();
				jedis.set("product_avail_" + orderProduct.getIdProduct(), newAvailability.toString());

				Product product = productRepository.getOne(orderProduct.getIdProduct());
				product.setInventory(newAvailability);
				productRepository.saveAndFlush(product);
			}
			ObjectMapper mapper = new ObjectMapper();
			jedis.lpush("allOrders", mapper.writeValueAsString(Util.avoidCicles(order)));
		}
		return true;
	}
	
	
}