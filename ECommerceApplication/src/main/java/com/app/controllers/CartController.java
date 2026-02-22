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
import com.app.security.AuthUtil;
import com.app.services.CartService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * Cart Controller - manages shopping cart operations.
 * 
 * SECURITY: All endpoints validate that the authenticated user can only access their own cart.
 * Admins can access any cart.
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

	@Autowired
	private AuthUtil authUtil;

	/**
	 * Add product to cart
	 * SECURITY: Users can only add to their own cart.
	 */
	@PostMapping("/public/carts/{cartId}/products/{productId}/quantity/{quantity}")
	public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long cartId, @PathVariable Long productId, @PathVariable Integer quantity) {
		authUtil.validateCartAccess(cartId);
		CartDTO cartDTO = cartService.addProductToCart(cartId, productId, quantity);
		
		return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.CREATED);
	}
	
	@GetMapping("/admin/carts")
	public ResponseEntity<List<CartDTO>> getCarts() {
		
		List<CartDTO> cartDTOs = cartService.getAllCarts();
		
		return new ResponseEntity<List<CartDTO>>(cartDTOs, HttpStatus.FOUND);
	}
	
	/**
	 * Get cart by email and cartId
	 * SECURITY: Users can only access their own cart.
	 */
	@GetMapping("/public/users/{email}/carts/{cartId}")
	public ResponseEntity<CartDTO> getCartById(@PathVariable String email, @PathVariable Long cartId) {
		authUtil.validateUserAccess(email);
		authUtil.validateCartAccess(cartId);
		CartDTO cartDTO = cartService.getCart(email, cartId);
		
		return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.FOUND);
	}
	
	/**
	 * Update product quantity in cart
	 * SECURITY: Users can only update their own cart.
	 */
	@PutMapping("/public/carts/{cartId}/products/{productId}/quantity/{quantity}")
	public ResponseEntity<CartDTO> updateCartProduct(@PathVariable Long cartId, @PathVariable Long productId, @PathVariable Integer quantity) {
		authUtil.validateCartAccess(cartId);
		CartDTO cartDTO = cartService.updateProductQuantityInCart(cartId, productId, quantity);
		
		return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
	}
	
	/**
	 * Delete product from cart
	 * SECURITY: Users can only delete from their own cart.
	 */
	@DeleteMapping("/public/carts/{cartId}/product/{productId}")
	public ResponseEntity<String> deleteProductFromCart(@PathVariable Long cartId, @PathVariable Long productId) {
		authUtil.validateCartAccess(cartId);
		String status = cartService.deleteProductFromCart(cartId, productId);
		
		return new ResponseEntity<String>(status, HttpStatus.OK);
	}

	/**
	 * Apply a promo code to cart.
	 * SECURITY: Users can only apply to their own cart.
	 * Feature can be disabled via app.feature.promo-code.enabled=false
	 */
	@PostMapping("/public/carts/{cartId}/promo/{promoCode}")
	public ResponseEntity<CartDTO> applyPromoCode(@PathVariable Long cartId, @PathVariable String promoCode) {
		if (!featureConfig.isPromoCodeEnabled()) {
			throw new APIException("Promo code feature is not enabled for this store");
		}
		authUtil.validateCartAccess(cartId);
		CartDTO cartDTO = cartService.applyPromoCode(cartId, promoCode);
		
		return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
	}

	/**
	 * Remove promo code from cart.
	 * SECURITY: Users can only remove from their own cart.
	 * Feature can be disabled via app.feature.promo-code.enabled=false
	 */
	@DeleteMapping("/public/carts/{cartId}/promo")
	public ResponseEntity<CartDTO> removePromoCode(@PathVariable Long cartId) {
		if (!featureConfig.isPromoCodeEnabled()) {
			throw new APIException("Promo code feature is not enabled for this store");
		}
		authUtil.validateCartAccess(cartId);
		CartDTO cartDTO = cartService.removePromoCode(cartId);
		
		return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
	}
}
