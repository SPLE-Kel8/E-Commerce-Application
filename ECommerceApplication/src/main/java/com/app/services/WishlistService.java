package com.app.services;

import com.app.payloads.WishlistDTO;

/**
 * Wishlist Service interface.
 * 
 * Provides methods for managing user wishlists.
 */
public interface WishlistService {

    /**
     * Get wishlist for a user by email
     */
    WishlistDTO getWishlistByUserEmail(String email);

    /**
     * Add a product to user's wishlist
     */
    WishlistDTO addProductToWishlist(String email, Long productId);

    /**
     * Remove a product from user's wishlist
     */
    WishlistDTO removeProductFromWishlist(String email, Long productId);

    /**
     * Check if a product exists in user's wishlist
     */
    boolean isProductInWishlist(String email, Long productId);

    /**
     * Move a product from wishlist to cart
     */
    WishlistDTO moveProductToCart(String email, Long productId, Integer quantity);

    /**
     * Clear all items from user's wishlist
     */
    String clearWishlist(String email);
}
