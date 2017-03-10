package com.cap.util;

import java.util.List;

import com.cap.models.Order;
import com.cap.models.OrderProduct;

public class Util {

	/**
	 * function used to avoid cicles on orders and products, since the lazy load
	 * of jpa does not work
	 * 
	 * @param orders
	 * @return
	 */
	public static List<Order> avoidCicles(List<Order> orders) {
		for (Order order : orders) {
			avoidCicles(order);
		}
		return orders;
	}

	public static Order avoidCicles(Order order) {
		for (OrderProduct orderProduct : order.getOrderProducts()) {
			orderProduct.setOrder(null);
		}
		return order;
	}
}
