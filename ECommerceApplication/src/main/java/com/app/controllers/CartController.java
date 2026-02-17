package com.app.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.config.FeatureConfig;
import com.app.exceptions.APIException;
import com.app.payloads.CartDTO;
import com.app.services.CartService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * Cart Controller - manages shopping cart operations.
 * 
 * Clone-and-Own Notes:
 * - Promo code functionality can be disabled via app.feature.promo-code.enabled=false
 * - Cart operations remain available regardless of promo code feature status
 */
@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "E-Commerce Application")
public class CartController {
	
	@Autowired
	private CartService cartService;

	@Autowired
	private FeatureConfig featureConfig;

	@PostMapping("/public/carts/{cartId}/products/{productId}/quantity/{quantity}")
	public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long cartId, @PathVariable Long productId, @PathVariable Integer quantity) {
		CartDTO cartDTO = cartService.addProductToCart(cartId, productId, quantity);
		
		return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.CREATED);
	}
	
	@GetMapping("/admin/carts")
	public ResponseEntity<List<CartDTO>> getCarts() {
		
		List<CartDTO> cartDTOs = cartService.getAllCarts();
		
		return new ResponseEntity<List<CartDTO>>(cartDTOs, HttpStatus.FOUND);
	}
	
	@GetMapping("/public/users/{email}/carts/{cartId}")
	public ResponseEntity<CartDTO> getCartById(@PathVariable String email, @PathVariable Long cartId) {
		CartDTO cartDTO = cartService.getCart(email, cartId);
		
		return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.FOUND);
	}
	
	@PutMapping("/public/carts/{cartId}/products/{productId}/quantity/{quantity}")
	public ResponseEntity<CartDTO> updateCartProduct(@PathVariable Long cartId, @PathVariable Long productId, @PathVariable Integer quantity) {
		CartDTO cartDTO = cartService.updateProductQuantityInCart(cartId, productId, quantity);
		
		return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
	}
	
	@DeleteMapping("/public/carts/{cartId}/product/{productId}")
	public ResponseEntity<String> deleteProductFromCart(@PathVariable Long cartId, @PathVariable Long productId) {
		String status = cartService.deleteProductFromCart(cartId, productId);
		
		return new ResponseEntity<String>(status, HttpStatus.OK);
	}

	/**
	 * Apply a promo code to cart.
	 * Feature can be disabled via app.feature.promo-code.enabled=false
	 */
	@PostMapping("/public/carts/{cartId}/promo/{promoCode}")
	public ResponseEntity<CartDTO> applyPromoCode(@PathVariable Long cartId, @PathVariable String promoCode) {
		if (!featureConfig.isPromoCodeEnabled()) {
			throw new APIException("Promo code feature is not enabled for this store");
		}
		CartDTO cartDTO = cartService.applyPromoCode(cartId, promoCode);
		
		return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
	}

	/**
	 * Remove promo code from cart.
	 * Feature can be disabled via app.feature.promo-code.enabled=false
	 */
	@DeleteMapping("/public/carts/{cartId}/promo")
	public ResponseEntity<CartDTO> removePromoCode(@PathVariable Long cartId) {
		if (!featureConfig.isPromoCodeEnabled()) {
			throw new APIException("Promo code feature is not enabled for this store");
		}
		CartDTO cartDTO = cartService.removePromoCode(cartId);
		
		return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
	}
}
