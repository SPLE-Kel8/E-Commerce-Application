package com.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.config.FeatureConfig;
import com.app.exceptions.APIException;
import com.app.payloads.WishlistDTO;
import com.app.security.AuthUtil;
import com.app.services.WishlistService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Wishlist Controller - manages wishlist operations.
 * 
 * SECURITY: All endpoints validate that the authenticated user can only access their own wishlist.
 * Admins can access any user's wishlist.
 * 
 * Clone-and-Own Notes:
 * - Wishlist functionality can be disabled via app.feature.wishlist.enabled=false
 * - When disabled, all endpoints will return 400 Bad Request
 */
@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "E-Commerce Application")
@Tag(name = "Wishlist", description = "Wishlist management APIs")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private FeatureConfig featureConfig;

    @Autowired
    private AuthUtil authUtil;

    /**
     * Check if wishlist feature is enabled
     */
    private void checkFeatureEnabled() {
        if (!featureConfig.isWishlistEnabled()) {
            throw new APIException("Wishlist feature is currently disabled");
        }
    }

    /**
     * Get user's wishlist
     * SECURITY: Users can only access their own wishlist. Admins can access any.
     */
    @GetMapping("/public/users/{email}/wishlist")
    @Operation(summary = "Get user's wishlist", description = "Retrieves user's wishlist with all saved products")
    public ResponseEntity<WishlistDTO> getWishlist(@PathVariable String email) {
        checkFeatureEnabled();
        authUtil.validateUserAccess(email);
        WishlistDTO wishlistDTO = wishlistService.getWishlistByUserEmail(email);
        return new ResponseEntity<>(wishlistDTO, HttpStatus.OK);
    }

    /**
     * Add product to wishlist
     * SECURITY: Users can only add to their own wishlist. Admins can add to any.
     */
    @PostMapping("/public/users/{email}/wishlist/products/{productId}")
    @Operation(summary = "Add product to wishlist", description = "Adds a product to user's wishlist")
    public ResponseEntity<WishlistDTO> addProductToWishlist(
            @PathVariable String email,
            @PathVariable Long productId) {
        checkFeatureEnabled();
        authUtil.validateUserAccess(email);
        WishlistDTO wishlistDTO = wishlistService.addProductToWishlist(email, productId);
        return new ResponseEntity<>(wishlistDTO, HttpStatus.CREATED);
    }

    /**
     * Remove product from wishlist
     * SECURITY: Users can only remove from their own wishlist. Admins can remove from any.
     */
    @DeleteMapping("/public/users/{email}/wishlist/products/{productId}")
    @Operation(summary = "Remove product from wishlist", description = "Removes a product from user's wishlist")
    public ResponseEntity<WishlistDTO> removeProductFromWishlist(
            @PathVariable String email,
            @PathVariable Long productId) {
        checkFeatureEnabled();
        authUtil.validateUserAccess(email);
        WishlistDTO wishlistDTO = wishlistService.removeProductFromWishlist(email, productId);
        return new ResponseEntity<>(wishlistDTO, HttpStatus.OK);
    }

    /**
     * Check if product is in wishlist
     * SECURITY: Users can only check their own wishlist. Admins can check any.
     */
    @GetMapping("/public/users/{email}/wishlist/products/{productId}/exists")
    @Operation(summary = "Check if product is in wishlist", description = "Returns true if product is in user's wishlist")
    public ResponseEntity<Boolean> isProductInWishlist(
            @PathVariable String email,
            @PathVariable Long productId) {
        checkFeatureEnabled();
        authUtil.validateUserAccess(email);
        boolean exists = wishlistService.isProductInWishlist(email, productId);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    /**
     * Move product from wishlist to cart
     * SECURITY: Users can only move from their own wishlist. Admins can move from any.
     */
    @PostMapping("/public/users/{email}/wishlist/products/{productId}/move-to-cart")
    @Operation(summary = "Move product to cart", description = "Moves a product from wishlist to cart with specified quantity")
    public ResponseEntity<WishlistDTO> moveProductToCart(
            @PathVariable String email,
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        checkFeatureEnabled();
        authUtil.validateUserAccess(email);
        WishlistDTO wishlistDTO = wishlistService.moveProductToCart(email, productId, quantity);
        return new ResponseEntity<>(wishlistDTO, HttpStatus.OK);
    }

    /**
     * Clear all items from wishlist
     * SECURITY: Users can only clear their own wishlist. Admins can clear any.
     */
    @DeleteMapping("/public/users/{email}/wishlist/clear")
    @Operation(summary = "Clear wishlist", description = "Removes all products from user's wishlist")
    public ResponseEntity<String> clearWishlist(@PathVariable String email) {
        checkFeatureEnabled();
        authUtil.validateUserAccess(email);
        String message = wishlistService.clearWishlist(email);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
}
