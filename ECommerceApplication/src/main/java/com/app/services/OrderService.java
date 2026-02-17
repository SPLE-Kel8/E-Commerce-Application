package com.app.services;

import java.util.List;

import com.app.payloads.BankAccountDTO;
import com.app.payloads.OrderDTO;
import com.app.payloads.OrderResponse;

public interface OrderService {
	
	OrderDTO placeOrder(String email, Long cartId, String paymentMethod);
	
	/**
	 * Place order with bank transfer payment (Requirement d).
	 * System returns bank account details for customer to transfer to.
	 */
	OrderDTO placeOrderWithBankTransfer(String email, Long cartId, BankAccountDTO bankAccount);
	
	OrderDTO getOrder(String email, Long orderId);
	
	List<OrderDTO> getOrdersByUser(String email);
	
	OrderResponse getAllOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
	
	OrderDTO updateOrder(String email, Long orderId, String orderStatus);
}
