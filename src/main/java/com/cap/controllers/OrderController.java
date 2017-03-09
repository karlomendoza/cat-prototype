package com.cap.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cap.models.Order;
import com.cap.models.OrderProduct;
import com.cap.models.OrderRepository;
import com.cap.models.SaleOrder;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

@RestController
@RequestMapping
public class OrderController {

	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private JedisPool jedisPool;
	
	@GetMapping
	@RequestMapping("/orderList")
	@CrossOrigin
	public List<Order> getOrders() {
		return avoidCicles(orderRepository.findAll());
	}
//	
//	@GetMapping
//	@RequestMapping("/getAllCached")
//	@CrossOrigin
//	public List<String> getOrdersCached() {
//		
//		List<Order> allOrders;
//		List<String> response = new ArrayList<>();
//		try(Jedis jedis = jedisPool.getResource()){
//			response = jedis.lrange("allOrders", 0, -1);
//			if(response == null || response.isEmpty()){
//				allOrders = orderRepository.findAll();
//				allOrders.forEach(order -> jedis.lpush("allOrders", order.toString()));
//				response = jedis.lrange("allOrders", 0, -1);
//			}
//		} catch (JedisConnectionException ex){
//			allOrders = orderRepository.findAll();
//			for (Order order: allOrders) {
//				response.add(order.toString());
//			}
//		}
//		return response;
//	}
//	
//	@Transactional
//	@GetMapping
//	@RequestMapping("/find/{id}")
//	@CrossOrigin
//	public Order getOrder(@PathVariable("id") int id) {
//		return avoidCicles(orderRepository.getOne(id));
//	}
	
	@Transactional
	@PostMapping
	@CrossOrigin
	@RequestMapping("/order")
//	public Order create(@PathVariable("id") Integer id,
//						@PathVariable("description") String description, 
//						@PathVariable("inventory") Integer inventory,
//						@PathVariable("price") Double price,
//						@PathVariable("products") List<SaleOrder> saleOrders) {

	public boolean create(){
		List<SaleOrder> saleOrders = new ArrayList<>();
		saleOrders.add(new SaleOrder(1, 2, 5.5));
		saleOrders.add(new SaleOrder(2, 2, 5.5));
		
		Integer id = (int) Math.floor(Math.random()*10000);
		Date date = new Date();
		Double total = 50D;
		String user = "karlo";
		
		
		////aquí empieza logica normal
		
		Order order = new Order(id, date, user, total);
		
		order.setOrderProducts(new ArrayList<>());
		for (SaleOrder saleOrder : saleOrders) {
			OrderProduct op = new OrderProduct();
			op.setOrder(order);
			op.setIdProduct(saleOrder.getIdProduct());
			op.setQuantity(saleOrder.getQuantity());
			op.setPrice(saleOrder.getPrice());
			order.getOrderProducts().add(op);
		}
		
		Order newOrder = orderRepository.saveAndFlush(order);
		
		try(Jedis jedis = jedisPool.getResource()){
			List<String> response = jedis.lrange("allOrders", 0, 1);
			if(response != null && !response.isEmpty()){
				jedis.lpush("allOrders", newOrder.toString());
			}
		}
		return true;
	}
	
	private List<Order> avoidCicles(List<Order> orders){
		for (Order order : orders) {
			avoidCicles(order);
		}
		return orders;
	}
	
	private Order avoidCicles(Order order){
		for (OrderProduct orderProduct : order.getOrderProducts()) {
			orderProduct.setOrder(null);
		}
		return order;
	}

}