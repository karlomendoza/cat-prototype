package com.cap.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cap.models.Order;
import com.cap.models.OrderProduct;
import com.cap.models.OrderProductRepository;
import com.cap.models.OrderRepository;
import com.cap.models.Product;
import com.cap.models.ProductRepository;
import com.cap.models.SaleOrder;
import com.cap.objects.SalesByProduct;
import com.cap.util.Util;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.ApiOperation;
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
	private OrderProductRepository orderproductRepository;

	@Autowired
	private JedisPool jedisPool;

	private static ObjectMapper mapper = new ObjectMapper();

	@CrossOrigin
	@RequestMapping(value = "/orderList", method = RequestMethod.GET)
	@ApiOperation(value = "Get a list of orders")
	public List<Order> getOrders() throws JsonParseException, JsonMappingException, IOException {
		List<String> allOrders = new ArrayList<>();
		List<Order> orders = new ArrayList<>();

		try (Jedis jedis = jedisPool.getResource()) {
			allOrders = jedis.lrange("allOrders", 0, -1);
		}
		for (String order : allOrders) {
			orders.add(mapper.readValue(order, Order.class));
		}

		return orders;
	}

	@CrossOrigin
	@RequestMapping(value = "/salesByDay", method = RequestMethod.GET)
	@ApiOperation(value = "Get a list of earnings by day")
	public Map<String, Double> getSalesByDay() throws JsonParseException, JsonMappingException, IOException {

		List<String> allOrders = new ArrayList<>();
		List<Order> orders = new ArrayList<>();

		try (Jedis jedis = jedisPool.getResource()) {
			allOrders = jedis.lrange("allOrders", 0, -1);
		}

		for (String order : allOrders) {
			orders.add(mapper.readValue(order, Order.class));
		}

		Map<String, Double> counting = orders.stream()
				.collect(Collectors.groupingBy(Order::getFormatedDate, Collectors.summingDouble(Order::getTotal)));

		return counting;
	}

	@CrossOrigin
	@RequestMapping(value = "/salesByProduct", method = RequestMethod.GET)
	@ApiOperation(value = "Get a list of sales by product, you get product, amount of product sold, and earnings by product")
	public List<SalesByProduct> getSalesByProduct() throws JsonParseException, JsonMappingException, IOException {

		List<OrderProduct> ops = orderproductRepository.findAll();
		Map<Integer, SalesByProduct> salesByProduct = new HashMap<>();

		SalesByProduct sbp;
		for (OrderProduct orderProduct : ops) {
			if (salesByProduct.containsKey(orderProduct.getIdProduct()))
				sbp = salesByProduct.get(orderProduct.getIdProduct());
			else {
				sbp = new SalesByProduct();
				try (Jedis jedis = jedisPool.getResource()) {
					String product = jedis.get("product_" + orderProduct.getIdProduct());
					sbp.setProductDescription(mapper.readValue(product, Product.class).getDescription());
				}
				salesByProduct.put(orderProduct.getIdProduct(), sbp);
			}
			sbp.addSale(orderProduct);
		}

		return new ArrayList<>(salesByProduct.values());
	}

	@Transactional
	@CrossOrigin
	@RequestMapping(method = RequestMethod.POST, value = "/order")
	@ApiOperation(value = "Create an order")
	public boolean create(@RequestBody SaleOrder saleOrder) throws IOException {

		// check availability && price, must be the same
		try (Jedis jedis = jedisPool.getResource()) {
			for (OrderProduct orderProduct : saleOrder.getProducts()) {
				Product product = mapper.readValue(jedis.get("product_" + orderProduct.getIdProduct()), Product.class);
				if (product.getInventory() < orderProduct.getQuantity()) {
					return false;
				}
				if (!product.getPrice().equals(orderProduct.getPrice())) {
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

		// check total amounts equals product price*quantity
		if (!validate(order))
			return false;

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

	private boolean validate(Order order) {
		Double total = order.getTotal();

		Double saleTotal = order.getOrderProducts().stream()
				.collect(Collectors.summingDouble(OrderProduct::getOrderProductSalesTotal));

		if (!total.equals(saleTotal))
			return false;

		return true;
	}

}