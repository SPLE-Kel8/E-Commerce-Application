package com.app.services;

import java.util.List;

import com.app.payloads.CartDTO;

public interface CartService {
	
	CartDTO addProductToCart(Long cartId, Long productId, Integer quantity);
	
	List<CartDTO> getAllCarts();
	
	CartDTO getCart(String email, Long cartId);
	
	CartDTO updateProductQuantityInCart(Long cartId, Long productId, Integer quantity);
	
	void updateProductInCarts(Long cartId, Long productId);
	
	String deleteProductFromCart(Long cartId, Long productId);
	
	CartDTO applyPromoCode(Long cartId, String promoCode);
	
	CartDTO removePromoCode(Long cartId);
	
}
