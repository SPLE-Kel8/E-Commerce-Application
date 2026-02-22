package com.app.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.app.config.FeatureConfig;
import com.app.entites.Category;
import com.app.entites.Product;
import com.app.entites.User;
import com.app.entites.Wishlist;
import com.app.entites.WishlistItem;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.WishlistDTO;
import com.app.repositories.ProductRepo;
import com.app.repositories.UserRepo;
import com.app.repositories.WishlistItemRepo;
import com.app.repositories.WishlistRepo;

/**
 * Test untuk WishlistService - fitur wishlist dengan feature toggling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WishlistService Tests - Wishlist Feature")
class WishlistServiceTest {

    @Mock
    private WishlistRepo wishlistRepo;

    @Mock
    private WishlistItemRepo wishlistItemRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private ProductRepo productRepo;

    @Mock
    private CartService cartService;

    @Mock
    private FeatureConfig featureConfig;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private User user;
    private Product product;
    private Wishlist wishlist;
    private WishlistItem wishlistItem;
    private Category category;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("TestUser");
        user.setLastName("Testing");

        category = new Category();
        category.setCategoryId(1L);
        category.setCategoryName("Electronics");

        product = new Product();
        product.setProductId(1L);
        product.setProductName("Laptop Gaming");
        product.setDescription("High performance gaming laptop");
        product.setPrice(15000000.0);
        product.setDiscount(10.0);
        product.setSpecialPrice(13500000.0);
        product.setQuantity(50);
        product.setImage("laptop.jpg");
        product.setCategory(category);

        wishlist = new Wishlist();
        wishlist.setWishlistId(1L);
        wishlist.setUser(user);
        wishlist.setWishlistItems(new ArrayList<>());
        wishlist.setCreatedAt(LocalDateTime.now());
        wishlist.setUpdatedAt(LocalDateTime.now());

        wishlistItem = new WishlistItem();
        wishlistItem.setWishlistItemId(1L);
        wishlistItem.setWishlist(wishlist);
        wishlistItem.setProduct(product);
        wishlistItem.setAddedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Feature Toggle Tests")
    class FeatureToggleTests {

        @Test
        @DisplayName("Should throw exception when wishlist feature is disabled")
        void shouldThrowExceptionWhenFeatureDisabled() {
            when(featureConfig.isWishlistEnabled()).thenReturn(false);

            APIException exception = assertThrows(APIException.class, () -> {
                wishlistService.getWishlistByUserEmail("test@example.com");
            });

            assertEquals("Wishlist feature is currently disabled", exception.getMessage());
        }

        @Test
        @DisplayName("Should work when wishlist feature is enabled")
        void shouldWorkWhenFeatureEnabled() {
            when(featureConfig.isWishlistEnabled()).thenReturn(true);
            when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(wishlistRepo.findByUserId(1L)).thenReturn(Optional.of(wishlist));

            WishlistDTO result = wishlistService.getWishlistByUserEmail("test@example.com");

            assertNotNull(result);
            assertEquals(1L, result.getWishlistId());
        }
    }

    @Nested
    @DisplayName("Get Wishlist Tests")
    class GetWishlistTests {

        @Test
        @DisplayName("Should return existing wishlist")
        void shouldReturnExistingWishlist() {
            when(featureConfig.isWishlistEnabled()).thenReturn(true);
            when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(wishlistRepo.findByUserId(1L)).thenReturn(Optional.of(wishlist));

            WishlistDTO result = wishlistService.getWishlistByUserEmail("test@example.com");

            assertNotNull(result);
            assertEquals(1L, result.getWishlistId());
            assertEquals(1L, result.getUserId());
        }

        @Test
        @DisplayName("Should create new wishlist if not exists")
        void shouldCreateNewWishlistIfNotExists() {
            when(featureConfig.isWishlistEnabled()).thenReturn(true);
            when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(wishlistRepo.findByUserId(1L)).thenReturn(Optional.empty());
            when(wishlistRepo.save(any(Wishlist.class))).thenReturn(wishlist);

            WishlistDTO result = wishlistService.getWishlistByUserEmail("test@example.com");

            assertNotNull(result);
            verify(wishlistRepo).save(any(Wishlist.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            when(featureConfig.isWishlistEnabled()).thenReturn(true);
            when(userRepo.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                wishlistService.getWishlistByUserEmail("notfound@example.com");
            });
        }
    }

    @Nested
    @DisplayName("Add Product to Wishlist Tests")
    class AddProductToWishlistTests {

        @Test
        @DisplayName("Should add product to wishlist successfully")
        void shouldAddProductToWishlistSuccessfully() {
            when(featureConfig.isWishlistEnabled()).thenReturn(true);
            when(featureConfig.getWishlistMaxItems()).thenReturn(50);
            when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(productRepo.findById(1L)).thenReturn(Optional.of(product));
            when(wishlistRepo.findByUserId(1L)).thenReturn(Optional.of(wishlist));
            when(wishlistItemRepo.existsByWishlistIdAndProductId(1L, 1L)).thenReturn(false);
            when(wishlistItemRepo.countByWishlistId(1L)).thenReturn(0);
            when(wishlistItemRepo.save(any(WishlistItem.class))).thenReturn(wishlistItem);

            WishlistDTO result = wishlistService.addProductToWishlist("test@example.com", 1L);

            assertNotNull(result);
            verify(wishlistItemRepo).save(any(WishlistItem.class));
        }

        @Test
        @DisplayName("Should throw exception when product already in wishlist")
        void shouldThrowExceptionWhenProductAlreadyInWishlist() {
            when(featureConfig.isWishlistEnabled()).thenReturn(true);
            when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(productRepo.findById(1L)).thenReturn(Optional.of(product));
            when(wishlistRepo.findByUserId(1L)).thenReturn(Optional.of(wishlist));
            when(wishlistItemRepo.existsByWishlistIdAndProductId(1L, 1L)).thenReturn(true);

            APIException exception = assertThrows(APIException.class, () -> {
                wishlistService.addProductToWishlist("test@example.com", 1L);
            });

            assertTrue(exception.getMessage().contains("already in your wishlist"));
        }

        @Test
        @DisplayName("Should throw exception when wishlist is full")
        void shouldThrowExceptionWhenWishlistIsFull() {
            when(featureConfig.isWishlistEnabled()).thenReturn(true);
            when(featureConfig.getWishlistMaxItems()).thenReturn(50);
            when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(productRepo.findById(1L)).thenReturn(Optional.of(product));
            when(wishlistRepo.findByUserId(1L)).thenReturn(Optional.of(wishlist));
            when(wishlistItemRepo.existsByWishlistIdAndProductId(1L, 1L)).thenReturn(false);
            when(wishlistItemRepo.countByWishlistId(1L)).thenReturn(50);

            APIException exception = assertThrows(APIException.class, () -> {
                wishlistService.addProductToWishlist("test@example.com", 1L);
            });

            assertTrue(exception.getMessage().contains("Wishlist is full"));
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowExceptionWhenProductNotFound() {
            when(featureConfig.isWishlistEnabled()).thenReturn(true);
            when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(productRepo.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                wishlistService.addProductToWishlist("test@example.com", 999L);
            });
        }
    }

    @Nested
    @DisplayName("Remove Product from Wishlist Tests")
    class RemoveProductFromWishlistTests {

        @Test
        @DisplayName("Should remove product from wishlist successfully")
        void shouldRemoveProductFromWishlistSuccessfully() {
            wishlist.getWishlistItems().add(wishlistItem);
            
            when(featureConfig.isWishlistEnabled()).thenReturn(true);
            when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(wishlistRepo.findByUserId(1L)).thenReturn(Optional.of(wishlist));
            when(wishlistItemRepo.findByWishlistIdAndProductId(1L, 1L)).thenReturn(Optional.of(wishlistItem));

            WishlistDTO result = wishlistService.removeProductFromWishlist("test@example.com", 1L);

            assertNotNull(result);
            verify(wishlistItemRepo).delete(wishlistItem);
        }

        @Test
        @DisplayName("Should throw exception when product not in wishlist")
        void shouldThrowExceptionWhenProductNotInWishlist() {
            when(featureConfig.isWishlistEnabled()).thenReturn(true);
            when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(wishlistRepo.findByUserId(1L)).thenReturn(Optional.of(wishlist));
            when(wishlistItemRepo.findByWishlistIdAndProductId(1L, 1L)).thenReturn(Optional.empty());

            APIException exception = assertThrows(APIException.class, () -> {
                wishlistService.removeProductFromWishlist("test@example.com", 1L);
            });

            assertEquals("Product not found in wishlist", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Check Product in Wishlist Tests")
    class CheckProductInWishlistTests {

        @Test
        @DisplayName("Should return true when product is in wishlist")
        void shouldReturnTrueWhenProductInWishlist() {
            when(featureConfig.isWishlistEnabled()).thenReturn(true);
            when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(wishlistRepo.findByUserId(1L)).thenReturn(Optional.of(wishlist));
            when(wishlistItemRepo.existsByWishlistIdAndProductId(1L, 1L)).thenReturn(true);

            boolean result = wishlistService.isProductInWishlist("test@example.com", 1L);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false when product is not in wishlist")
        void shouldReturnFalseWhenProductNotInWishlist() {
            when(featureConfig.isWishlistEnabled()).thenReturn(true);
            when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(wishlistRepo.findByUserId(1L)).thenReturn(Optional.of(wishlist));
            when(wishlistItemRepo.existsByWishlistIdAndProductId(1L, 1L)).thenReturn(false);

            boolean result = wishlistService.isProductInWishlist("test@example.com", 1L);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Clear Wishlist Tests")
    class ClearWishlistTests {

        @Test
        @DisplayName("Should clear wishlist successfully")
        void shouldClearWishlistSuccessfully() {
            wishlist.getWishlistItems().add(wishlistItem);
            
            when(featureConfig.isWishlistEnabled()).thenReturn(true);
            when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(wishlistRepo.findByUserId(1L)).thenReturn(Optional.of(wishlist));

            String result = wishlistService.clearWishlist("test@example.com");

            assertEquals("Wishlist cleared successfully", result);
            verify(wishlistItemRepo).deleteByWishlistWishlistId(1L);
        }
    }
}
