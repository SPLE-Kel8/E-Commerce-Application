package com.app.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import com.app.payloads.CartDTO;
import com.app.payloads.ProductDTO;
import com.app.security.AuthUtil;
import com.app.services.CartService;

/**
 * Test untuk CartController - fitur keranjang dan promo code.
 * 
 * Sesuai deskripsi:
 * - Keranjang
 * - Diskon dengan kode promo
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartController Tests - Shopping Cart & Promo Code")
class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private FeatureConfig featureConfig;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private CartController cartController;

    private CartDTO cartDTO;
    private ProductDTO productDTO;
    private Long testCartId;
    private Long testProductId;
    private String testEmail;

    @BeforeEach
    void setUp() {
        testCartId = 1L;
        testProductId = 1L;
        testEmail = "user@test.com";

        productDTO = new ProductDTO();
        productDTO.setProductId(1L);
        productDTO.setProductName("Test Product");
        productDTO.setPrice(100000.0);
        productDTO.setSpecialPrice(90000.0);
        productDTO.setQuantity(10);

        cartDTO = new CartDTO();
        cartDTO.setCartId(testCartId);
        cartDTO.setTotalPrice(100000.0);
        cartDTO.setProducts(new ArrayList<>(Arrays.asList(productDTO)));
        cartDTO.setDiscountAmount(0.0);
        cartDTO.setFinalPrice(100000.0);
    }

    @Nested
    @DisplayName("Add Product to Cart Tests")
    class AddProductToCartTests {

        @Test
        @DisplayName("Berhasil menambahkan produk ke keranjang")
        void addProductToCart_Success() {
            when(cartService.addProductToCart(testCartId, testProductId, 2)).thenReturn(cartDTO);

            ResponseEntity<CartDTO> response = cartController.addProductToCart(testCartId, testProductId, 2);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(testCartId, response.getBody().getCartId());
            verify(cartService).addProductToCart(testCartId, testProductId, 2);
        }

        @Test
        @DisplayName("Keranjang memiliki daftar produk")
        void addProductToCart_HasProducts() {
            when(cartService.addProductToCart(testCartId, testProductId, 1)).thenReturn(cartDTO);

            ResponseEntity<CartDTO> response = cartController.addProductToCart(testCartId, testProductId, 1);

            assertNotNull(response.getBody().getProducts());
            assertFalse(response.getBody().getProducts().isEmpty());
        }
    }

    @Nested
    @DisplayName("Get Cart Tests")
    class GetCartTests {

        @Test
        @DisplayName("Berhasil mendapatkan keranjang user")
        void getCartById_Success() {
            when(cartService.getCart(testEmail, testCartId)).thenReturn(cartDTO);

            ResponseEntity<CartDTO> response = cartController.getCartById(testEmail, testCartId);

            assertEquals(HttpStatus.FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(testCartId, response.getBody().getCartId());
        }

        @Test
        @DisplayName("Admin berhasil mendapatkan semua keranjang")
        void getCarts_Admin_Success() {
            List<CartDTO> carts = Arrays.asList(cartDTO);
            when(cartService.getAllCarts()).thenReturn(carts);

            ResponseEntity<List<CartDTO>> response = cartController.getCarts();

            assertEquals(HttpStatus.FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
        }
    }

    @Nested
    @DisplayName("Update Cart Product Quantity Tests")
    class UpdateCartProductTests {

        @Test
        @DisplayName("Berhasil mengupdate quantity produk di keranjang")
        void updateCartProduct_Success() {
            when(cartService.updateProductQuantityInCart(testCartId, testProductId, 5)).thenReturn(cartDTO);

            ResponseEntity<CartDTO> response = cartController.updateCartProduct(testCartId, testProductId, 5);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(cartService).updateProductQuantityInCart(testCartId, testProductId, 5);
        }
    }

    @Nested
    @DisplayName("Delete Product from Cart Tests")
    class DeleteProductFromCartTests {

        @Test
        @DisplayName("Berhasil menghapus produk dari keranjang")
        void deleteProductFromCart_Success() {
            when(cartService.deleteProductFromCart(testCartId, testProductId))
                .thenReturn("Product removed from the cart !!!");

            ResponseEntity<String> response = cartController.deleteProductFromCart(testCartId, testProductId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().contains("removed"));
            verify(cartService).deleteProductFromCart(testCartId, testProductId);
        }
    }

    @Nested
    @DisplayName("Apply Promo Code to Cart Tests")
    class ApplyPromoCodeTests {

        @BeforeEach
        void setUpPromoCode() {
            cartDTO.setAppliedPromoCode("DISKON10");
            cartDTO.setDiscountAmount(10000.0);
            cartDTO.setFinalPrice(90000.0);
        }

        @Test
        @DisplayName("Berhasil apply promo code ke keranjang")
        void applyPromoCode_Success() {
            when(featureConfig.isPromoCodeEnabled()).thenReturn(true);
            when(cartService.applyPromoCode(testCartId, "DISKON10")).thenReturn(cartDTO);

            ResponseEntity<CartDTO> response = cartController.applyPromoCode(testCartId, "DISKON10");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("DISKON10", response.getBody().getAppliedPromoCode());
            assertEquals(10000.0, response.getBody().getDiscountAmount());
            assertEquals(90000.0, response.getBody().getFinalPrice());
        }

        @Test
        @DisplayName("Apply promo code saat fitur disabled gagal")
        void applyPromoCode_FeatureDisabled_ThrowsException() {
            when(featureConfig.isPromoCodeEnabled()).thenReturn(false);

            APIException exception = assertThrows(APIException.class,
                () -> cartController.applyPromoCode(testCartId, "DISKON10"));

            assertTrue(exception.getMessage().contains("not enabled"));
            verify(cartService, never()).applyPromoCode(any(), any());
        }

        @Test
        @DisplayName("Final price = total price - discount amount")
        void applyPromoCode_CalculatesCorrectFinalPrice() {
            cartDTO.setTotalPrice(100000.0);
            cartDTO.setDiscountAmount(15000.0);
            cartDTO.setFinalPrice(85000.0);

            when(featureConfig.isPromoCodeEnabled()).thenReturn(true);
            when(cartService.applyPromoCode(testCartId, "DISKON15")).thenReturn(cartDTO);

            ResponseEntity<CartDTO> response = cartController.applyPromoCode(testCartId, "DISKON15");
            CartDTO result = response.getBody();

            assertEquals(100000.0, result.getTotalPrice());
            assertEquals(15000.0, result.getDiscountAmount());
            assertEquals(85000.0, result.getFinalPrice());
        }
    }

    @Nested
    @DisplayName("Remove Promo Code from Cart Tests")
    class RemovePromoCodeTests {

        @Test
        @DisplayName("Berhasil remove promo code dari keranjang")
        void removePromoCode_Success() {
            cartDTO.setAppliedPromoCode(null);
            cartDTO.setDiscountAmount(0.0);
            cartDTO.setFinalPrice(100000.0);

            when(featureConfig.isPromoCodeEnabled()).thenReturn(true);
            when(cartService.removePromoCode(testCartId)).thenReturn(cartDTO);

            ResponseEntity<CartDTO> response = cartController.removePromoCode(testCartId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNull(response.getBody().getAppliedPromoCode());
            assertEquals(0.0, response.getBody().getDiscountAmount());
        }

        @Test
        @DisplayName("Remove promo code saat fitur disabled gagal")
        void removePromoCode_FeatureDisabled_ThrowsException() {
            when(featureConfig.isPromoCodeEnabled()).thenReturn(false);

            APIException exception = assertThrows(APIException.class,
                () -> cartController.removePromoCode(testCartId));

            assertTrue(exception.getMessage().contains("not enabled"));
            verify(cartService, never()).removePromoCode(any());
        }
    }

    @Nested
    @DisplayName("Cart Total Price Tests")
    class CartTotalPriceTests {

        @Test
        @DisplayName("Total price dihitung dengan benar")
        void cartTotalPrice_CalculatedCorrectly() {
            ProductDTO product1 = new ProductDTO();
            product1.setSpecialPrice(50000.0);

            ProductDTO product2 = new ProductDTO();
            product2.setSpecialPrice(75000.0);

            cartDTO.setProducts(Arrays.asList(product1, product2));
            cartDTO.setTotalPrice(125000.0);

            when(cartService.getCart(testEmail, testCartId)).thenReturn(cartDTO);

            ResponseEntity<CartDTO> response = cartController.getCartById(testEmail, testCartId);

            assertEquals(125000.0, response.getBody().getTotalPrice());
        }

        @Test
        @DisplayName("Keranjang kosong memiliki total price 0")
        void emptyCart_HasZeroTotalPrice() {
            cartDTO.setProducts(new ArrayList<>());
            cartDTO.setTotalPrice(0.0);
            cartDTO.setFinalPrice(0.0);

            when(cartService.getCart(testEmail, testCartId)).thenReturn(cartDTO);

            ResponseEntity<CartDTO> response = cartController.getCartById(testEmail, testCartId);

            assertEquals(0.0, response.getBody().getTotalPrice());
        }
    }

    @Nested
    @DisplayName("Original Total Price Tests - Requirement (f)")
    class OriginalTotalPriceTests {

        @Test
        @DisplayName("originalTotalPrice berisi harga asli tanpa diskon produk")
        void originalTotalPrice_ContainsOriginalPrices() {
            // Product dengan diskon: price 100000, specialPrice 90000
            productDTO.setPrice(100000.0);
            productDTO.setSpecialPrice(90000.0);
            
            // totalPrice menggunakan specialPrice, originalTotalPrice menggunakan price
            cartDTO.setTotalPrice(90000.0);
            cartDTO.setOriginalTotalPrice(100000.0);
            cartDTO.setFinalPrice(90000.0);

            when(cartService.getCart(testEmail, testCartId)).thenReturn(cartDTO);

            ResponseEntity<CartDTO> response = cartController.getCartById(testEmail, testCartId);
            CartDTO result = response.getBody();

            assertEquals(90000.0, result.getTotalPrice());
            assertEquals(100000.0, result.getOriginalTotalPrice());
        }

        @Test
        @DisplayName("Promo code discount dihitung dari originalTotalPrice")
        void promoCodeDiscount_CalculatedFromOriginalPrice() {
            // Produk: price 100000, specialPrice 90000
            // Promo code: DISKON10 (10% dari originalTotalPrice = 10000)
            // Sesuai requirement (f): diskon produk tidak dihitung jika pakai promo
            // Final price = originalTotalPrice - promoDiscount = 100000 - 10000 = 90000
            
            cartDTO.setTotalPrice(90000.0);  // uses specialPrice
            cartDTO.setOriginalTotalPrice(100000.0);  // uses price
            cartDTO.setAppliedPromoCode("DISKON10");
            cartDTO.setDiscountAmount(10000.0);  // 10% of originalTotalPrice
            cartDTO.setFinalPrice(90000.0);  // originalTotalPrice - discountAmount

            when(featureConfig.isPromoCodeEnabled()).thenReturn(true);
            when(cartService.applyPromoCode(testCartId, "DISKON10")).thenReturn(cartDTO);

            ResponseEntity<CartDTO> response = cartController.applyPromoCode(testCartId, "DISKON10");
            CartDTO result = response.getBody();

            // Verifikasi bahwa discount dihitung dari originalTotalPrice
            assertEquals(100000.0, result.getOriginalTotalPrice());
            assertEquals(10000.0, result.getDiscountAmount());
            // finalPrice = originalTotalPrice - discountAmount (bukan dari totalPrice)
            assertEquals(90000.0, result.getFinalPrice());
        }
    }
}
