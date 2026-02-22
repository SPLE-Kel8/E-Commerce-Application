package com.app.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.config.AppConstants;
import com.app.config.FeatureConfig;
import com.app.exceptions.APIException;
import com.app.payloads.BankAccountDTO;
import com.app.payloads.OrderDTO;
import com.app.payloads.OrderResponse;
import com.app.security.AuthUtil;
import com.app.services.OrderService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * Order Controller - handles order placement and management.
 * 
 * SECURITY: All endpoints validate that the authenticated user can only access their own orders.
 * Admins can access any order.
 * 
 * Clone-and-Own Notes:
 * - Payment methods are configured via FeatureConfig (application.properties)
 * - Bank transfer requires bank selection (Requirement d)
 */
@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "E-Commerce Application")
public class OrderController {
	
	@Autowired
	public OrderService orderService;
	
	@Autowired
	private FeatureConfig featureConfig;

	@Autowired
	private AuthUtil authUtil;

	/**
	 * Get list of supported banks for bank transfer (Requirement d).
	 * Customer can select from this list when placing order.
	 */
	@GetMapping("/public/payments/banks")
	public ResponseEntity<List<BankAccountDTO>> getSupportedBanks() {
		if (!featureConfig.isBankTransferEnabled()) {
			throw new APIException("Bank transfer payment is not enabled");
		}
		
		List<BankAccountDTO> banks = featureConfig.getSupportedBankList();
		
		// Hide account numbers in public listing, show only on order confirmation
		banks.forEach(bank -> {
			bank.setAccountNumber(null);
			bank.setAccountName(null);
		});
		
		return new ResponseEntity<>(banks, HttpStatus.OK);
	}
	
	/**
	 * Place an order with bank transfer payment (Requirement d).
	 * Customer selects bank, system returns account number to transfer to.
	 * SECURITY: Users can only place orders for themselves.
	 * 
	 * @param email User email
	 * @param cartId Cart ID
	 * @param bankCode Bank code (e.g., BCA, BNI, MANDIRI)
	 */
	@PostMapping("/public/users/{email}/carts/{cartId}/payments/bank-transfer/{bankCode}/order")
	public ResponseEntity<OrderDTO> orderWithBankTransfer(
			@PathVariable String email, 
			@PathVariable Long cartId, 
			@PathVariable String bankCode) {
		
		// SECURITY: Validate user can only place orders for themselves
		authUtil.validateUserAccess(email);
		authUtil.validateCartAccess(cartId);
		
		// Validate bank transfer is enabled
		if (!featureConfig.isBankTransferEnabled()) {
			throw new APIException("Bank transfer payment is not enabled");
		}
		
		// Validate bank code is supported (Requirement d)
		if (!featureConfig.isBankSupported(bankCode)) {
			throw new APIException(featureConfig.getSupportedBanksMessage());
		}
		
		// Get bank account details
		BankAccountDTO bankAccount = featureConfig.getBankAccount(bankCode);
		
		OrderDTO order = orderService.placeOrderWithBankTransfer(email, cartId, bankAccount);
		
		return new ResponseEntity<>(order, HttpStatus.CREATED);
	}
	
	/**
	 * Legacy endpoint - Place an order with payment method validation.
	 * SECURITY: Users can only place orders for themselves.
	 * For bank transfer, use /bank-transfer/{bankCode}/order endpoint instead.
	 */
	@PostMapping("/public/users/{email}/carts/{cartId}/payments/{paymentMethod}/order")
	public ResponseEntity<OrderDTO> orderProducts(
			@PathVariable String email, 
			@PathVariable Long cartId, 
			@PathVariable String paymentMethod) {
		
		// SECURITY: Validate user can only place orders for themselves
		authUtil.validateUserAccess(email);
		authUtil.validateCartAccess(cartId);
		
		// Validate payment method using FeatureConfig
		if (!featureConfig.isPaymentMethodEnabled(paymentMethod)) {
			throw new APIException(featureConfig.getSupportedPaymentMethodsMessage());
		}
		
		// For bank transfer, redirect to use specific endpoint
		if ("BANK_TRANSFER".equalsIgnoreCase(paymentMethod)) {
			throw new APIException("For bank transfer, please use /payments/bank-transfer/{bankCode}/order endpoint. " 
				+ featureConfig.getSupportedBanksMessage());
		}
		
		OrderDTO order = orderService.placeOrder(email, cartId, paymentMethod);
		
		return new ResponseEntity<>(order, HttpStatus.CREATED);
	}

	@GetMapping("/admin/orders")
	public ResponseEntity<OrderResponse> getAllOrders(
			@RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
			@RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
			@RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_ORDERS_BY, required = false) String sortBy,
			@RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
		
		OrderResponse orderResponse = orderService.getAllOrders(pageNumber, pageSize, sortBy, sortOrder);

		return new ResponseEntity<OrderResponse>(orderResponse, HttpStatus.FOUND);
	}
	
	/**
	 * Get orders by user
	 * SECURITY: Users can only view their own orders.
	 */
	@GetMapping("public/users/{email}/orders")
	public ResponseEntity<List<OrderDTO>> getOrdersByUser(@PathVariable String email) {
		authUtil.validateUserAccess(email);
		List<OrderDTO> orders = orderService.getOrdersByUser(email);
		
		return new ResponseEntity<List<OrderDTO>>(orders, HttpStatus.FOUND);
	}
	
	/**
	 * Get specific order by user
	 * SECURITY: Users can only view their own orders.
	 */
	@GetMapping("public/users/{email}/orders/{orderId}")
	public ResponseEntity<OrderDTO> getOrderByUser(@PathVariable String email, @PathVariable Long orderId) {
		authUtil.validateUserAccess(email);
		OrderDTO order = orderService.getOrder(email, orderId);
		
		return new ResponseEntity<OrderDTO>(order, HttpStatus.FOUND);
	}
	
	@PutMapping("admin/users/{email}/orders/{orderId}/orderStatus/{orderStatus}")
	public ResponseEntity<OrderDTO> updateOrderByUser(@PathVariable String email, @PathVariable Long orderId, @PathVariable String orderStatus) {
		OrderDTO order = orderService.updateOrder(email, orderId, orderStatus);
		
		return new ResponseEntity<OrderDTO>(order, HttpStatus.OK);
	}

}
