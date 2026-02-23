package com.app.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
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

import com.app.payloads.CreateProductReviewDTO;
import com.app.payloads.ProductReviewDTO;
import com.app.payloads.ProductReviewResponse;
import com.app.security.AuthUtil;
import com.app.services.ProductReviewService;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductReviewController Tests - Product Review Endpoint")
class ProductReviewControllerTest {

	@Mock
	private ProductReviewService productReviewService;

	@Mock
	private AuthUtil authUtil;

	@InjectMocks
	private ProductReviewController productReviewController;

	private ProductReviewDTO productReviewDTO;
	private ProductReviewResponse productReviewResponse;
	private CreateProductReviewDTO createProductReviewDTO;

	@BeforeEach
	void setUp() {
		productReviewDTO = new ProductReviewDTO();
		productReviewDTO.setReviewId(100L);
		productReviewDTO.setProductId(10L);
		productReviewDTO.setUserId(1L);
		productReviewDTO.setUserEmail("user@test.com");
		productReviewDTO.setRating(5);
		productReviewDTO.setComment("Produk bagus sekali");
		productReviewDTO.setCreatedAt(LocalDateTime.now());
		productReviewDTO.setUpdatedAt(LocalDateTime.now());

		productReviewResponse = new ProductReviewResponse();
		productReviewResponse.setContent(Arrays.asList(productReviewDTO));
		productReviewResponse.setPageNumber(0);
		productReviewResponse.setPageSize(5);
		productReviewResponse.setTotalElements(1L);
		productReviewResponse.setTotalPages(1);
		productReviewResponse.setLastPage(true);
		productReviewResponse.setAverageRating(5.0);
		productReviewResponse.setReviewCount(1L);

		createProductReviewDTO = new CreateProductReviewDTO();
		createProductReviewDTO.setRating(5);
		createProductReviewDTO.setComment("Produk bagus sekali");
	}

	@Nested
	@DisplayName("Get Reviews Tests")
	class GetReviewsTests {

		@Test
		@DisplayName("Berhasil mendapatkan review produk")
		void getReviewsByProduct_Success() {
			when(productReviewService.getReviewsByProduct(10L, 0, 5, "createdAt", "desc"))
					.thenReturn(productReviewResponse);

			ResponseEntity<ProductReviewResponse> response = productReviewController.getReviewsByProduct(10L, 0, 5,
					"createdAt", "desc");

			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(1L, response.getBody().getReviewCount());
		}
	}

	@Nested
	@DisplayName("Create Review Tests")
	class CreateReviewTests {

		@Test
		@DisplayName("Berhasil membuat review")
		void createReview_Success() {
			doNothing().when(authUtil).validateUserAccess("user@test.com");
			when(productReviewService.createReview("user@test.com", 10L, createProductReviewDTO)).thenReturn(productReviewDTO);

			ResponseEntity<ProductReviewDTO> response = productReviewController.createReview("user@test.com", 10L,
					createProductReviewDTO);

			assertEquals(HttpStatus.CREATED, response.getStatusCode());
			assertNotNull(response.getBody());
			verify(authUtil).validateUserAccess("user@test.com");
			verify(productReviewService).createReview("user@test.com", 10L, createProductReviewDTO);
		}
	}

	@Nested
	@DisplayName("Get My Review Tests")
	class GetMyReviewTests {

		@Test
		@DisplayName("Berhasil mendapatkan review milik user")
		void getMyReviewForProduct_Success() {
			doNothing().when(authUtil).validateUserAccess("user@test.com");
			when(productReviewService.getReviewByUserAndProduct("user@test.com", 10L)).thenReturn(productReviewDTO);

			ResponseEntity<ProductReviewDTO> response = productReviewController.getMyReviewForProduct("user@test.com", 10L);

			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(100L, response.getBody().getReviewId());
		}
	}

	@Nested
	@DisplayName("Update Review Tests")
	class UpdateReviewTests {

		@Test
		@DisplayName("Berhasil update review")
		void updateReview_Success() {
			ProductReviewDTO updated = new ProductReviewDTO();
			updated.setReviewId(100L);
			updated.setProductId(10L);
			updated.setUserId(1L);
			updated.setUserEmail("user@test.com");
			updated.setRating(4);
			updated.setComment("Update review");

			doNothing().when(authUtil).validateUserAccess("user@test.com");
			when(productReviewService.updateReview("user@test.com", 10L, 100L, createProductReviewDTO)).thenReturn(updated);

			ResponseEntity<ProductReviewDTO> response = productReviewController.updateReview("user@test.com", 10L, 100L,
					createProductReviewDTO);

			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertEquals(4, response.getBody().getRating());
		}
	}

	@Nested
	@DisplayName("Delete Review Tests")
	class DeleteReviewTests {

		@Test
		@DisplayName("Berhasil delete review")
		void deleteReview_Success() {
			doNothing().when(authUtil).validateUserAccess("user@test.com");
			when(productReviewService.deleteReview("user@test.com", 10L, 100L)).thenReturn("Review deleted successfully");

			ResponseEntity<String> response = productReviewController.deleteReview("user@test.com", 10L, 100L);

			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertEquals("Review deleted successfully", response.getBody());
			verify(productReviewService).deleteReview("user@test.com", 10L, 100L);
		}
	}
}