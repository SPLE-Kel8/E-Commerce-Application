package com.app.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.app.config.AppConstants;
import com.app.entites.Cart;
import com.app.entites.CartItem;
import com.app.entites.Order;
import com.app.entites.OrderItem;
import com.app.entites.Payment;
import com.app.entites.Product;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.BankAccountDTO;
import com.app.payloads.OrderDTO;
import com.app.payloads.OrderItemDTO;
import com.app.payloads.OrderResponse;
import com.app.repositories.CartItemRepo;
import com.app.repositories.CartRepo;
import com.app.repositories.OrderItemRepo;
import com.app.repositories.OrderRepo;
import com.app.repositories.PaymentRepo;
import com.app.repositories.PromoCodeRepo;
import com.app.repositories.UserRepo;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	public UserRepo userRepo;

	@Autowired
	public CartRepo cartRepo;

	@Autowired
	public OrderRepo orderRepo;

	@Autowired
	private PaymentRepo paymentRepo;

	@Autowired
	private PromoCodeRepo promoCodeRepo;

	@Autowired
	public OrderItemRepo orderItemRepo;

	@Autowired
	public CartItemRepo cartItemRepo;

	@Autowired
	public UserService userService;

	@Autowired
	public CartService cartService;

	@Autowired
	private PromoCodeUsageHistoryService promoCodeUsageHistoryService;

	@Autowired
	private com.app.repositories.PromoCodeUsageHistoryRepo promoCodeUsageHistoryRepo;

	@Autowired
	public ModelMapper modelMapper;

	@Override
	public OrderDTO placeOrder(String email, Long cartId, String paymentMethod) {

		Cart cart = cartRepo.findCartByEmailAndCartId(email, cartId);

		if (cart == null) {
			throw new ResourceNotFoundException("Cart", "cartId", cartId);
		}

		Order order = new Order();

		order.setEmail(email);
		order.setOrderDate(LocalDate.now());

		// Use finalPrice which includes any promo code discounts
		Double finalAmount = cart.getFinalPrice() != null && cart.getFinalPrice() > 0 
				? cart.getFinalPrice() 
				: cart.getTotalPrice();
		order.setTotalAmount(finalAmount);
		order.setOrderStatus(AppConstants.ORDER_STATUS_ACCEPTED);
		
		// Set promo code info if applied
		if (cart.getAppliedPromoCode() != null) {
			order.setAppliedPromoCode(cart.getAppliedPromoCode().getCode());
			order.setDiscountAmount(cart.getDiscountAmount() != null ? cart.getDiscountAmount() : 0.0);
		}

		Payment payment = new Payment();
		payment.setOrder(order);
		payment.setPaymentMethod(paymentMethod);

		payment = paymentRepo.save(payment);

		order.setPayment(payment);

		Order savedOrder = orderRepo.save(order);

		List<CartItem> cartItems = cart.getCartItems();

		if (cartItems.size() == 0) {
			throw new APIException("Cart is empty");
		}

		List<OrderItem> orderItems = new ArrayList<>();

		for (CartItem cartItem : cartItems) {
			OrderItem orderItem = new OrderItem();

			orderItem.setProduct(cartItem.getProduct());
			orderItem.setQuantity(cartItem.getQuantity());
			orderItem.setDiscount(cartItem.getDiscount());
			orderItem.setOrderedProductPrice(cartItem.getProductPrice());
			orderItem.setOrder(savedOrder);

			orderItems.add(orderItem);
		}

		orderItems = orderItemRepo.saveAll(orderItems);

		// Update promo code usage count if applied
		if (cart.getAppliedPromoCode() != null) {
			cart.getAppliedPromoCode().setUsedCount(cart.getAppliedPromoCode().getUsedCount() + 1);
			promoCodeRepo.save(cart.getAppliedPromoCode());
		}

		cart.getCartItems().forEach(item -> {
			int quantity = item.getQuantity();

			Product product = item.getProduct();

			cartService.deleteProductFromCart(cartId, item.getProduct().getProductId());

			product.setQuantity(product.getQuantity() - quantity);
		});

		// Reset cart promo code after order is placed
		cart.setAppliedPromoCode(null);
		cart.setDiscountAmount(0.0);
		cart.setFinalPrice(0.0);

		OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
		
		orderItems.forEach(item -> orderDTO.getOrderItems().add(modelMapper.map(item, OrderItemDTO.class)));

		return orderDTO;
	}

	/**
	 * Place order with bank transfer payment (Requirement d).
	 * Customer selects bank, system returns account number to transfer to.
	 */
	@Override
	public OrderDTO placeOrderWithBankTransfer(String email, Long cartId, BankAccountDTO bankAccount) {
		Cart cart = cartRepo.findCartByEmailAndCartId(email, cartId);

		if (cart == null) {
			throw new ResourceNotFoundException("Cart", "cartId", cartId);
		}

		Order order = new Order();

		order.setEmail(email);
		order.setOrderDate(LocalDate.now());

		// Use finalPrice which includes any promo code discounts
		Double finalAmount = cart.getFinalPrice() != null && cart.getFinalPrice() > 0 
				? cart.getFinalPrice() 
				: cart.getTotalPrice();
		order.setTotalAmount(finalAmount);
		order.setOrderStatus(AppConstants.ORDER_STATUS_ACCEPTED);
		
		// Set promo code info if applied
		if (cart.getAppliedPromoCode() != null) {
			order.setAppliedPromoCode(cart.getAppliedPromoCode().getCode());
			order.setDiscountAmount(cart.getDiscountAmount() != null ? cart.getDiscountAmount() : 0.0);
		}

		// Create payment with bank transfer details (Requirement d)
		Payment payment = new Payment();
		payment.setOrder(order);
		payment.setPaymentMethod("BANK_TRANSFER");
		payment.setBankCode(bankAccount.getBankCode());
		payment.setBankName(bankAccount.getBankName());
		payment.setBankAccountNumber(bankAccount.getAccountNumber());
		payment.setBankAccountName(bankAccount.getAccountName());

		payment = paymentRepo.save(payment);

		order.setPayment(payment);

		Order savedOrder = orderRepo.save(order);

		List<CartItem> cartItems = cart.getCartItems();

		if (cartItems.size() == 0) {
			throw new APIException("Cart is empty");
		}

		List<OrderItem> orderItems = new ArrayList<>();

		for (CartItem cartItem : cartItems) {
			OrderItem orderItem = new OrderItem();

			orderItem.setProduct(cartItem.getProduct());
			orderItem.setQuantity(cartItem.getQuantity());
			orderItem.setDiscount(cartItem.getDiscount());
			orderItem.setOrderedProductPrice(cartItem.getProductPrice());
			orderItem.setOrder(savedOrder);

			orderItems.add(orderItem);
		}

		orderItems = orderItemRepo.saveAll(orderItems);

		// Update promo code usage count if applied
		if (cart.getAppliedPromoCode() != null) {
			cart.getAppliedPromoCode().setUsedCount(cart.getAppliedPromoCode().getUsedCount() + 1);
			promoCodeRepo.save(cart.getAppliedPromoCode());
			
			// Record promo code usage history
			com.app.entites.User user = userRepo.findByEmail(email)
					.orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
			
			com.app.entites.PromoCodeUsageHistory history = new com.app.entites.PromoCodeUsageHistory();
			history.setUser(user);
			history.setPromoCode(cart.getAppliedPromoCode());
			history.setOrder(savedOrder);
			history.setPromoCodeUsed(cart.getAppliedPromoCode().getCode());
			history.setDiscountAmount(cart.getDiscountAmount() != null ? cart.getDiscountAmount() : 0.0);
			history.setOrderAmount(cart.getTotalPrice());
			history.setFinalAmount(finalAmount);
			history.setUsedAt(java.time.LocalDateTime.now());
			history.setStatus("APPLIED");
			
			// Save history directly to repository (avoid ModelMapper ambiguity)
			promoCodeUsageHistoryRepo.save(history);
		}

		cart.getCartItems().forEach(item -> {
			int quantity = item.getQuantity();

			Product product = item.getProduct();

			cartService.deleteProductFromCart(cartId, item.getProduct().getProductId());

			product.setQuantity(product.getQuantity() - quantity);
		});

		// Reset cart promo code after order is placed
		cart.setAppliedPromoCode(null);
		cart.setDiscountAmount(0.0);
		cart.setFinalPrice(0.0);

		OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
		
		orderItems.forEach(item -> orderDTO.getOrderItems().add(modelMapper.map(item, OrderItemDTO.class)));

		return orderDTO;
	}

	@Override
	public List<OrderDTO> getOrdersByUser(String email) {
		List<Order> orders = orderRepo.findAllByEmail(email);

		List<OrderDTO> orderDTOs = orders.stream().map(order -> modelMapper.map(order, OrderDTO.class))
				.collect(Collectors.toList());

		if (orderDTOs.size() == 0) {
			throw new APIException("No orders placed yet by the user with email: " + email);
		}

		return orderDTOs;
	}

	@Override
	public OrderDTO getOrder(String email, Long orderId) {

		Order order = orderRepo.findOrderByEmailAndOrderId(email, orderId);

		if (order == null) {
			throw new ResourceNotFoundException("Order", "orderId", orderId);
		}

		return modelMapper.map(order, OrderDTO.class);
	}

	@Override
	public OrderResponse getAllOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

		Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
				: Sort.by(sortBy).descending();

		Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

		Page<Order> pageOrders = orderRepo.findAll(pageDetails);

		List<Order> orders = pageOrders.getContent();

		List<OrderDTO> orderDTOs = orders.stream().map(order -> modelMapper.map(order, OrderDTO.class))
				.collect(Collectors.toList());
		
		if (orderDTOs.size() == 0) {
			throw new APIException("No orders placed yet by the users");
		}

		OrderResponse orderResponse = new OrderResponse();
		
		orderResponse.setContent(orderDTOs);
		orderResponse.setPageNumber(pageOrders.getNumber());
		orderResponse.setPageSize(pageOrders.getSize());
		orderResponse.setTotalElements(pageOrders.getTotalElements());
		orderResponse.setTotalPages(pageOrders.getTotalPages());
		orderResponse.setLastPage(pageOrders.isLast());
		
		return orderResponse;
	}

	@Override
	public OrderDTO updateOrder(String email, Long orderId, String orderStatus) {

		Order order = orderRepo.findOrderByEmailAndOrderId(email, orderId);

		if (order == null) {
			throw new ResourceNotFoundException("Order", "orderId", orderId);
		}

		order.setOrderStatus(orderStatus);

		return modelMapper.map(order, OrderDTO.class);
	}

}
