package com.app.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.app.config.FeatureConfig;
import com.app.exceptions.APIException;
import com.app.payloads.WishlistDTO;
import com.app.payloads.WishlistItemDTO;
import com.app.security.AuthUtil;
import com.app.services.WishlistService;

/**
 * Test untuk WishlistController - fitur wishlist dengan feature toggling dan security.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WishlistController Tests - Wishlist Feature")
class WishlistControllerTest {

    @Mock
    private WishlistService wishlistService;

    @Mock
    private FeatureConfig featureConfig;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private WishlistController wishlistController;

    private WishlistDTO wishlistDTO;
    private WishlistItemDTO wishlistItemDTO;
    private String testEmail;
    private Long testProductId;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testProductId = 1L;

        wishlistItemDTO = new WishlistItemDTO();
        wishlistItemDTO.setWishlistItemId(1L);
        wishlistItemDTO.setProductId(1L);
        wishlistItemDTO.setProductName("Laptop Gaming");
        wishlistItemDTO.setPrice(15000000.0);
        wishlistItemDTO.setSpecialPrice(13500000.0);
        wishlistItemDTO.setInStock(true);
        wishlistItemDTO.setAddedAt(LocalDateTime.now());

        wishlistDTO = new WishlistDTO();
        wishlistDTO.setWishlistId(1L);
        wishlistDTO.setUserId(1L);
        wishlistDTO.setTotalItems(1);
        wishlistDTO.setItems(new ArrayList<>(Arrays.asList(wishlistItemDTO)));
        wishlistDTO.setCreatedAt(LocalDateTime.now());
        wishlistDTO.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Feature Toggle Tests")
    class FeatureToggleTests {

        @Test
        @DisplayName("Should throw exception when wishlist feature is disabled")
        void shouldThrowExceptionWhenFeatureDisabled() {
            when(featureConfig.isWishlistEnabled()).thenReturn(false);

            APIException exception = assertThrows(APIException.class, () -> {
                wishlistController.getWishlist(testEmail);
            });

            assertEquals("Wishlist feature is currently disabled", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Should throw exception when user tries to access another user's wishlist")
        void shouldThrowExceptionWhenAccessDenied() {
            when(featureConfig.isWishlistEnabled()).thenReturn(true);
            doThrow(new APIException("Access denied: You can only access your own resources"))
                .when(authUtil).validateUserAccess(anyString());

            APIException exception = assertThrows(APIException.class, () -> {
                wishlistController.getWishlist("other@example.com");
            });

            assertEquals("Access denied: You can only access your own resources", exception.getMessage());
        }

        @Test
        @DisplayName("Should allow access when user accesses their own wishlist")
        void shouldAllowAccessWhenOwnWishlist() {
            when(featureConfig.isWishlistEnabled()).thenReturn(true);
            doNothing().when(authUtil).validateUserAccess(testEmail);
            when(wishlistService.getWishlistByUserEmail(testEmail)).thenReturn(wishlistDTO);

            ResponseEntity<WishlistDTO> response = wishlistController.getWishlist(testEmail);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(authUtil).validateUserAccess(testEmail);
        }
    }

    @Nested
    @DisplayName("Get Wishlist Tests")
    class GetWishlistTests {

        @Test
        @DisplayName("Should return wishlist successfully")
        void shouldReturnWishlistSuccessfully() {
            when(featureConfig.isWishlistEnabled()).thenReturn(true);
            when(wishlistService.getWishlistByUserEmail(testEmail)).thenReturn(wishlistDTO);

            ResponseEntity<WishlistDTO> response = wishlistController.getWishlist(testEmail);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1L, response.getBody().getWishlistId());
            assertEquals(1, response.getBody().getTotalItems());
        }
    }

    @Nested
    @DisplayName("Add Product to Wishlist Tests")
    class AddProductToWishlistTests {

        @Test
        @DisplayName("Should add product to wishlist successfully")
        void shouldAddProductToWishlistSuccessfully() {
            when(featureConfig.isWishlistEnabled()).thenReturn(true);
            when(wishlistService.addProductToWishlist(testEmail, testProductId)).thenReturn(wishlistDTO);

            ResponseEntity<WishlistDTO> response = wishlistController.addProductToWishlist(testEmail, testProductId);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(wishlistService).addProductToWishlist(testEmail, testProductId);
        }
    }

    @Nested
    @DisplayName("Remove Product from Wishlist Tests")
    class RemoveProductFromWishlistTests {

        @Test
        @DisplayName("Should remove product from wishlist successfully")
        void shouldRemoveProductFromWishlistSuccessfully() {
            WishlistDTO emptyWishlist = new WishlistDTO();
            emptyWishlist.setWishlistId(1L);
            emptyWishlist.setTotalItems(0);
            emptyWishlist.setItems(new ArrayList<>());

            when(featureConfig.isWishlistEnabled()).thenReturn(true);
            when(wishlistService.removeProductFromWishlist(testEmail, testProductId)).thenReturn(emptyWishlist);

            ResponseEntity<WishlistDTO> response = wishlistController.removeProductFromWishlist(testEmail, testProductId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(0, response.getBody().getTotalItems());
        }
    }

    @Nested
    @DisplayName("Check Product in Wishlist Tests")
    class CheckProductInWishlistTests {

        @Test
        @DisplayName("Should return true when product is in wishlist")
        void shouldReturnTrueWhenProductInWishlist() {
            when(featureConfig.isWishlistEnabled()).thenReturn(true);
            when(wishlistService.isProductInWishlist(testEmail, testProductId)).thenReturn(true);

            ResponseEntity<Boolean> response = wishlistController.isProductInWishlist(testEmail, testProductId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody());
        }

        @Test
        @DisplayName("Should return false when product is not in wishlist")
        void shouldReturnFalseWhenProductNotInWishlist() {
            when(featureConfig.isWishlistEnabled()).thenReturn(true);
            when(wishlistService.isProductInWishlist(testEmail, testProductId)).thenReturn(false);

            ResponseEntity<Boolean> response = wishlistController.isProductInWishlist(testEmail, testProductId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertFalse(response.getBody());
        }
    }

    @Nested
    @DisplayName("Move Product to Cart Tests")
    class MoveProductToCartTests {

        @Test
        @DisplayName("Should move product to cart successfully")
        void shouldMoveProductToCartSuccessfully() {
            WishlistDTO updatedWishlist = new WishlistDTO();
            updatedWishlist.setWishlistId(1L);
            updatedWishlist.setTotalItems(0);
            updatedWishlist.setItems(new ArrayList<>());

            when(featureConfig.isWishlistEnabled()).thenReturn(true);
            when(wishlistService.moveProductToCart(testEmail, testProductId, 1)).thenReturn(updatedWishlist);

            ResponseEntity<WishlistDTO> response = wishlistController.moveProductToCart(testEmail, testProductId, 1);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(wishlistService).moveProductToCart(testEmail, testProductId, 1);
        }
    }

    @Nested
    @DisplayName("Clear Wishlist Tests")
    class ClearWishlistTests {

        @Test
        @DisplayName("Should clear wishlist successfully")
        void shouldClearWishlistSuccessfully() {
            when(featureConfig.isWishlistEnabled()).thenReturn(true);
            when(wishlistService.clearWishlist(testEmail)).thenReturn("Wishlist cleared successfully");

            ResponseEntity<String> response = wishlistController.clearWishlist(testEmail);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Wishlist cleared successfully", response.getBody());
        }
    }
}
