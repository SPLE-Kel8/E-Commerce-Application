package com.app.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.config.FeatureConfig;
import com.app.entites.Product;
import com.app.entites.User;
import com.app.entites.Wishlist;
import com.app.entites.WishlistItem;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.WishlistDTO;
import com.app.payloads.WishlistItemDTO;
import com.app.repositories.ProductRepo;
import com.app.repositories.UserRepo;
import com.app.repositories.WishlistItemRepo;
import com.app.repositories.WishlistRepo;

import jakarta.transaction.Transactional;

/**
 * Wishlist Service Implementation.
 * 
 * Provides functionality for managing user wishlists.
 * This feature can be disabled via app.feature.wishlist.enabled=false
 */
@Transactional
@Service
public class WishlistServiceImpl implements WishlistService {

    @Autowired
    private WishlistRepo wishlistRepo;

    @Autowired
    private WishlistItemRepo wishlistItemRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private CartService cartService;

    @Autowired
    private FeatureConfig featureConfig;

    /**
     * Check if wishlist feature is enabled, throw exception if not
     */
    private void checkWishlistFeatureEnabled() {
        if (!featureConfig.isWishlistEnabled()) {
            throw new APIException("Wishlist feature is currently disabled");
        }
    }

    /**
     * Get or create wishlist for a user
     */
    private Wishlist getOrCreateWishlist(User user) {
        return wishlistRepo.findByUserId(user.getUserId())
                .orElseGet(() -> {
                    Wishlist newWishlist = new Wishlist();
                    newWishlist.setUser(user);
                    newWishlist.setCreatedAt(LocalDateTime.now());
                    newWishlist.setUpdatedAt(LocalDateTime.now());
                    return wishlistRepo.save(newWishlist);
                });
    }

    /**
     * Convert Wishlist entity to WishlistDTO
     */
    private WishlistDTO convertToDTO(Wishlist wishlist) {
        WishlistDTO dto = new WishlistDTO();
        dto.setWishlistId(wishlist.getWishlistId());
        dto.setUserId(wishlist.getUser().getUserId());
        dto.setCreatedAt(wishlist.getCreatedAt());
        dto.setUpdatedAt(wishlist.getUpdatedAt());

        List<WishlistItemDTO> items = wishlist.getWishlistItems().stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());

        dto.setItems(items);
        dto.setTotalItems(items.size());

        return dto;
    }

    /**
     * Convert WishlistItem entity to WishlistItemDTO
     */
    private WishlistItemDTO convertItemToDTO(WishlistItem item) {
        Product product = item.getProduct();
        
        WishlistItemDTO dto = new WishlistItemDTO();
        dto.setWishlistItemId(item.getWishlistItemId());
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setDescription(product.getDescription());
        dto.setImage(product.getImage());
        dto.setPrice(product.getPrice());
        dto.setDiscount(product.getDiscount());
        dto.setSpecialPrice(product.getSpecialPrice());
        dto.setInStock(product.getQuantity() > 0);
        dto.setCategoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null);
        dto.setAddedAt(item.getAddedAt());

        return dto;
    }

    @Override
    public WishlistDTO getWishlistByUserEmail(String email) {
        checkWishlistFeatureEnabled();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        Wishlist wishlist = getOrCreateWishlist(user);
        return convertToDTO(wishlist);
    }

    @Override
    public WishlistDTO addProductToWishlist(String email, Long productId) {
        checkWishlistFeatureEnabled();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        Wishlist wishlist = getOrCreateWishlist(user);

        // Check if product already in wishlist
        boolean exists = wishlistItemRepo.existsByWishlistIdAndProductId(wishlist.getWishlistId(), productId);
        if (exists) {
            throw new APIException("Product '" + product.getProductName() + "' is already in your wishlist");
        }

        // Check max items limit
        int currentCount = wishlistItemRepo.countByWishlistId(wishlist.getWishlistId());
        if (currentCount >= featureConfig.getWishlistMaxItems()) {
            throw new APIException("Wishlist is full. Maximum " + featureConfig.getWishlistMaxItems() + " items allowed");
        }

        // Add item to wishlist
        WishlistItem wishlistItem = new WishlistItem();
        wishlistItem.setWishlist(wishlist);
        wishlistItem.setProduct(product);
        wishlistItem.setAddedAt(LocalDateTime.now());

        wishlistItemRepo.save(wishlistItem);

        // Update wishlist timestamp
        wishlist.setUpdatedAt(LocalDateTime.now());
        wishlist.getWishlistItems().add(wishlistItem);

        return convertToDTO(wishlist);
    }

    @Override
    public WishlistDTO removeProductFromWishlist(String email, Long productId) {
        checkWishlistFeatureEnabled();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        Wishlist wishlist = wishlistRepo.findByUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist", "userId", user.getUserId()));

        WishlistItem wishlistItem = wishlistItemRepo.findByWishlistIdAndProductId(wishlist.getWishlistId(), productId)
                .orElseThrow(() -> new APIException("Product not found in wishlist"));

        wishlist.getWishlistItems().remove(wishlistItem);
        wishlistItemRepo.delete(wishlistItem);

        // Update wishlist timestamp
        wishlist.setUpdatedAt(LocalDateTime.now());

        return convertToDTO(wishlist);
    }

    @Override
    public boolean isProductInWishlist(String email, Long productId) {
        checkWishlistFeatureEnabled();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return wishlistRepo.findByUserId(user.getUserId())
                .map(wishlist -> wishlistItemRepo.existsByWishlistIdAndProductId(wishlist.getWishlistId(), productId))
                .orElse(false);
    }

    @Override
    public WishlistDTO moveProductToCart(String email, Long productId, Integer quantity) {
        checkWishlistFeatureEnabled();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        Wishlist wishlist = wishlistRepo.findByUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist", "userId", user.getUserId()));

        WishlistItem wishlistItem = wishlistItemRepo.findByWishlistIdAndProductId(wishlist.getWishlistId(), productId)
                .orElseThrow(() -> new APIException("Product not found in wishlist"));

        // Add to cart (using user's cart)
        if (user.getCart() != null) {
            cartService.addProductToCart(user.getCart().getCartId(), productId, quantity);
        } else {
            throw new APIException("User does not have a cart. Please create a cart first.");
        }

        // Remove from wishlist
        wishlist.getWishlistItems().remove(wishlistItem);
        wishlistItemRepo.delete(wishlistItem);
        wishlist.setUpdatedAt(LocalDateTime.now());

        return convertToDTO(wishlist);
    }

    @Override
    public String clearWishlist(String email) {
        checkWishlistFeatureEnabled();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        Wishlist wishlist = wishlistRepo.findByUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist", "userId", user.getUserId()));

        wishlist.getWishlistItems().clear();
        wishlistItemRepo.deleteByWishlistWishlistId(wishlist.getWishlistId());
        wishlist.setUpdatedAt(LocalDateTime.now());

        return "Wishlist cleared successfully";
    }
}
