package com.app.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.app.entites.Product;
import com.app.entites.ProductReview;
import com.app.entites.User;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.CreateProductReviewDTO;
import com.app.payloads.ProductReviewDTO;
import com.app.payloads.ProductReviewResponse;
import com.app.repositories.OrderItemRepo;
import com.app.repositories.ProductRepo;
import com.app.repositories.ProductReviewRepo;
import com.app.repositories.UserRepo;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductReviewService Tests - Product Review")
class ProductReviewServiceTest {

	@Mock
	private ProductReviewRepo productReviewRepo;

	@Mock
	private ProductRepo productRepo;

	@Mock
	private UserRepo userRepo;

	@Mock
	private OrderItemRepo orderItemRepo;

	@InjectMocks
	private ProductReviewServiceImpl productReviewService;

	private User user;
	private Product product;
	private ProductReview review;
	private CreateProductReviewDTO createReviewDTO;

	@BeforeEach
	void setUp() {
		user = new User();
		user.setUserId(1L);
		user.setEmail("user@test.com");

		product = new Product();
		product.setProductId(10L);
		product.setProductName("Laptop");

		review = new ProductReview();
		review.setReviewId(100L);
		review.setUser(user);
		review.setProduct(product);
		review.setRating(5);
		review.setComment("Produk sangat bagus");
		review.setCreatedAt(LocalDateTime.now());
		review.setUpdatedAt(LocalDateTime.now());

		createReviewDTO = new CreateProductReviewDTO();
		createReviewDTO.setRating(5);
		createReviewDTO.setComment("Produk sangat bagus");
	}

	@Nested
	@DisplayName("Create Review Tests")
	class CreateReviewTests {

		@Test
		@DisplayName("Berhasil membuat review untuk produk yang pernah dibeli")
		void createReview_Success() {
			when(userRepo.findByEmail("user@test.com")).thenReturn(Optional.of(user));
			when(productRepo.findById(10L)).thenReturn(Optional.of(product));
			when(orderItemRepo.existsByOrderEmailAndProductProductId("user@test.com", 10L)).thenReturn(true);
			when(productReviewRepo.existsByProductProductIdAndUserUserId(10L, 1L)).thenReturn(false);
			when(productReviewRepo.save(any(ProductReview.class))).thenReturn(review);

			ProductReviewDTO result = productReviewService.createReview("user@test.com", 10L, createReviewDTO);

			assertNotNull(result);
			assertEquals(5, result.getRating());
			assertEquals("Produk sangat bagus", result.getComment());
			verify(productReviewRepo).save(any(ProductReview.class));
		}

		@Test
		@DisplayName("Gagal membuat review jika user belum pernah membeli produk")
		void createReview_NotPurchased_ThrowsException() {
			when(userRepo.findByEmail("user@test.com")).thenReturn(Optional.of(user));
			when(productRepo.findById(10L)).thenReturn(Optional.of(product));
			when(orderItemRepo.existsByOrderEmailAndProductProductId("user@test.com", 10L)).thenReturn(false);

			APIException exception = assertThrows(APIException.class,
					() -> productReviewService.createReview("user@test.com", 10L, createReviewDTO));

			assertTrue(exception.getMessage().contains("have purchased"));
			verify(productReviewRepo, never()).save(any(ProductReview.class));
		}

		@Test
		@DisplayName("Gagal membuat review duplikat untuk produk yang sama")
		void createReview_Duplicate_ThrowsException() {
			when(userRepo.findByEmail("user@test.com")).thenReturn(Optional.of(user));
			when(productRepo.findById(10L)).thenReturn(Optional.of(product));
			when(orderItemRepo.existsByOrderEmailAndProductProductId("user@test.com", 10L)).thenReturn(true);
			when(productReviewRepo.existsByProductProductIdAndUserUserId(10L, 1L)).thenReturn(true);

			APIException exception = assertThrows(APIException.class,
					() -> productReviewService.createReview("user@test.com", 10L, createReviewDTO));

			assertTrue(exception.getMessage().contains("already reviewed"));
		}
	}

	@Nested
	@DisplayName("Get Reviews Tests")
	class GetReviewsTests {

		@Test
		@DisplayName("Berhasil mendapatkan daftar review dengan ringkasan rating")
		void getReviewsByProduct_Success() {
			Page<ProductReview> page = new PageImpl<>(Arrays.asList(review));

			when(productRepo.findById(10L)).thenReturn(Optional.of(product));
			when(productReviewRepo.findByProductProductId(eq(10L), any(Pageable.class))).thenReturn(page);
			when(productReviewRepo.findAverageRatingByProductId(10L)).thenReturn(4.5);
			when(productReviewRepo.countByProductProductId(10L)).thenReturn(1L);

			ProductReviewResponse result = productReviewService.getReviewsByProduct(10L, 0, 5, "createdAt", "desc");

			assertNotNull(result);
			assertEquals(1, result.getContent().size());
			assertEquals(4.5, result.getAverageRating());
			assertEquals(1L, result.getReviewCount());
		}

		@Test
		@DisplayName("Average rating default ke 0.0 saat belum ada rata-rata")
		void getReviewsByProduct_AverageNull_DefaultZero() {
			Page<ProductReview> page = new PageImpl<>(new ArrayList<>());

			when(productRepo.findById(10L)).thenReturn(Optional.of(product));
			when(productReviewRepo.findByProductProductId(eq(10L), any(Pageable.class))).thenReturn(page);
			when(productReviewRepo.findAverageRatingByProductId(10L)).thenReturn(null);
			when(productReviewRepo.countByProductProductId(10L)).thenReturn(0L);

			ProductReviewResponse result = productReviewService.getReviewsByProduct(10L, 0, 5, "createdAt", "desc");

			assertNotNull(result);
			assertEquals(0.0, result.getAverageRating());
			assertEquals(0L, result.getReviewCount());
		}
	}

	@Nested
	@DisplayName("Update Review Tests")
	class UpdateReviewTests {

		@Test
		@DisplayName("Berhasil update review milik sendiri")
		void updateReview_Success() {
			CreateProductReviewDTO updateDTO = new CreateProductReviewDTO(4, "Setelah dipakai seminggu, masih bagus");

			when(userRepo.findByEmail("user@test.com")).thenReturn(Optional.of(user));
			when(productRepo.findById(10L)).thenReturn(Optional.of(product));
			when(productReviewRepo.findById(100L)).thenReturn(Optional.of(review));
			when(productReviewRepo.save(any(ProductReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

			ProductReviewDTO result = productReviewService.updateReview("user@test.com", 10L, 100L, updateDTO);

			assertNotNull(result);
			assertEquals(4, result.getRating());
			assertEquals("Setelah dipakai seminggu, masih bagus", result.getComment());
		}

		@Test
		@DisplayName("Gagal update review milik user lain")
		void updateReview_NotOwner_ThrowsException() {
			User anotherUser = new User();
			anotherUser.setUserId(2L);
			anotherUser.setEmail("other@test.com");

			when(userRepo.findByEmail("other@test.com")).thenReturn(Optional.of(anotherUser));
			when(productRepo.findById(10L)).thenReturn(Optional.of(product));
			when(productReviewRepo.findById(100L)).thenReturn(Optional.of(review));

			APIException exception = assertThrows(APIException.class,
					() -> productReviewService.updateReview("other@test.com", 10L, 100L, createReviewDTO));

			assertTrue(exception.getMessage().contains("own review"));
		}
	}

	@Nested
	@DisplayName("Delete Review Tests")
	class DeleteReviewTests {

		@Test
		@DisplayName("Berhasil menghapus review milik sendiri")
		void deleteReview_Success() {
			when(userRepo.findByEmail("user@test.com")).thenReturn(Optional.of(user));
			when(productRepo.findById(10L)).thenReturn(Optional.of(product));
			when(productReviewRepo.findById(100L)).thenReturn(Optional.of(review));
			doNothing().when(productReviewRepo).delete(review);

			String result = productReviewService.deleteReview("user@test.com", 10L, 100L);

			assertEquals("Review deleted successfully", result);
			verify(productReviewRepo).delete(review);
		}

		@Test
		@DisplayName("Gagal menghapus review jika review tidak ditemukan")
		void deleteReview_NotFound_ThrowsException() {
			when(userRepo.findByEmail("user@test.com")).thenReturn(Optional.of(user));
			when(productRepo.findById(10L)).thenReturn(Optional.of(product));
			when(productReviewRepo.findById(999L)).thenReturn(Optional.empty());

			assertThrows(ResourceNotFoundException.class,
					() -> productReviewService.deleteReview("user@test.com", 10L, 999L));
		}
	}
}