package com.cap.objects;

import com.cap.models.OrderProduct;

public class SalesByProduct {
	
	private String productDescription;
	
	private Integer quantity = 0;
	
	private Double sales = 0D;
	
	public void addSale(OrderProduct op){
		this.quantity += op.getQuantity();
		this.sales += op.getPrice();
	}


	public String getProductDescription() {
		return productDescription;
	}



	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}



	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Double getSales() {
		return sales;
	}

	public void setSales(Double sales) {
		this.sales = sales;
	}
	
	

}
