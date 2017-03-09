package com.cap.models;

public class SaleOrder {

	private Integer idProduct;
	
	private Integer quantity;
	
	private Double price;

	public SaleOrder(Integer idProduct, Integer quantity, Double price) {
		this.idProduct = idProduct;
		this.quantity = quantity;
		this.price = price;
	}

	public Integer getIdProduct() {
		return idProduct;
	}

	public void setIdProduct(Integer idProduct) {
		this.idProduct = idProduct;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}
}
