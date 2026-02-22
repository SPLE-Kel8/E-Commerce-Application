package com.app.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.app.entites.User;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.repositories.CartRepo;
import com.app.repositories.UserRepo;

/**
 * Authentication utility service for security validations.
 * 
 * Provides methods to:
 * - Get current authenticated user
 * - Validate user access to resources
 * - Check if current user is admin
 */
@Component
public class AuthUtil {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CartRepo cartRepo;

    /**
     * Get the email of the currently authenticated user
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new APIException("User not authenticated");
        }
        return (String) authentication.getPrincipal();
    }

    /**
     * Get the currently authenticated user entity
     */
    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    /**
     * Get the user ID of the currently authenticated user
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    /**
     * Check if the current user is an admin
     */
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN"));
    }

    /**
     * Validate that the current user has access to the resource identified by email.
     * Admins can access any user's resources.
     * Regular users can only access their own resources.
     * 
     * @param resourceOwnerEmail The email of the resource owner
     * @throws APIException if user doesn't have access
     */
    public void validateUserAccess(String resourceOwnerEmail) {
        if (isAdmin()) {
            return; // Admins have access to all resources
        }
        
        String currentEmail = getCurrentUserEmail();
        if (!currentEmail.equals(resourceOwnerEmail)) {
            throw new APIException("Access denied: You can only access your own resources");
        }
    }

    /**
     * Validate that the current user has access to the resource identified by userId.
     * Admins can access any user's resources.
     * Regular users can only access their own resources.
     * 
     * @param resourceOwnerId The userId of the resource owner
     * @throws APIException if user doesn't have access
     */
    public void validateUserAccess(Long resourceOwnerId) {
        if (isAdmin()) {
            return; // Admins have access to all resources
        }
        
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(resourceOwnerId)) {
            throw new APIException("Access denied: You can only access your own resources");
        }
    }

    /**
     * Validate that the current user owns the cart.
     * Admins can access any cart.
     * 
     * @param cartId The cartId to validate
     * @throws APIException if user doesn't own the cart
     */
    public void validateCartAccess(Long cartId) {
        if (isAdmin()) {
            return; // Admins have access to all carts
        }
        
        String cartOwnerEmail = cartRepo.findUserEmailByCartId(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        
        String currentEmail = getCurrentUserEmail();
        if (!currentEmail.equals(cartOwnerEmail)) {
            throw new APIException("Access denied: You can only access your own cart");
        }
    }
}
