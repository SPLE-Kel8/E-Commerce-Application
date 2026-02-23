package com.app.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductReviewServiceImpl implements ProductReviewService {

	@Autowired
	private ProductReviewRepo productReviewRepo;

	@Autowired
	private ProductRepo productRepo;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private OrderItemRepo orderItemRepo;

	private ProductReviewDTO mapToDTO(ProductReview review) {
		ProductReviewDTO dto = new ProductReviewDTO();
		dto.setReviewId(review.getReviewId());
		dto.setProductId(review.getProduct().getProductId());
		dto.setUserId(review.getUser().getUserId());
		dto.setUserEmail(review.getUser().getEmail());
		dto.setRating(review.getRating());
		dto.setComment(review.getComment());
		dto.setCreatedAt(review.getCreatedAt());
		dto.setUpdatedAt(review.getUpdatedAt());
		return dto;
	}

	private User getUserByEmail(String email) {
		return userRepo.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
	}

	private Product getProductById(Long productId) {
		return productRepo.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
	}

	private void validateProductPurchased(String email, Long productId) {
		boolean hasPurchased = orderItemRepo.existsByOrderEmailAndProductProductId(email, productId);
		if (!hasPurchased) {
			throw new APIException("You can only review products that you have purchased");
		}
	}

	@Override
	public ProductReviewDTO createReview(String email, Long productId, CreateProductReviewDTO reviewDTO) {
		User user = getUserByEmail(email);
		Product product = getProductById(productId);

		validateProductPurchased(email, productId);

		boolean reviewAlreadyExists = productReviewRepo.existsByProductProductIdAndUserUserId(productId, user.getUserId());
		if (reviewAlreadyExists) {
			throw new APIException("You have already reviewed this product");
		}

		ProductReview review = new ProductReview();
		review.setProduct(product);
		review.setUser(user);
		review.setRating(reviewDTO.getRating());
		review.setComment(reviewDTO.getComment());
		review.setCreatedAt(LocalDateTime.now());
		review.setUpdatedAt(LocalDateTime.now());

		ProductReview savedReview = productReviewRepo.save(review);
		return mapToDTO(savedReview);
	}

	@Override
	public ProductReviewResponse getReviewsByProduct(Long productId, Integer pageNumber, Integer pageSize, String sortBy,
			String sortOrder) {
		getProductById(productId);

		Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
				: Sort.by(sortBy).descending();

		Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
		Page<ProductReview> reviewPage = productReviewRepo.findByProductProductId(productId, pageDetails);

		List<ProductReviewDTO> reviewDTOs = reviewPage.getContent().stream().map(this::mapToDTO)
				.collect(Collectors.toList());

		Double averageRating = productReviewRepo.findAverageRatingByProductId(productId);
		long reviewCount = productReviewRepo.countByProductProductId(productId);

		ProductReviewResponse response = new ProductReviewResponse();
		response.setContent(reviewDTOs);
		response.setPageNumber(reviewPage.getNumber());
		response.setPageSize(reviewPage.getSize());
		response.setTotalElements(reviewPage.getTotalElements());
		response.setTotalPages(reviewPage.getTotalPages());
		response.setLastPage(reviewPage.isLast());
		response.setAverageRating(averageRating == null ? 0.0 : averageRating);
		response.setReviewCount(reviewCount);

		return response;
	}

	@Override
	public ProductReviewDTO getReviewByUserAndProduct(String email, Long productId) {
		User user = getUserByEmail(email);
		getProductById(productId);

		ProductReview review = productReviewRepo.findByProductProductIdAndUserUserId(productId, user.getUserId())
				.orElseThrow(() -> new ResourceNotFoundException("ProductReview", "productId", productId));

		return mapToDTO(review);
	}

	@Override
	public ProductReviewDTO updateReview(String email, Long productId, Long reviewId, CreateProductReviewDTO reviewDTO) {
		User user = getUserByEmail(email);
		getProductById(productId);

		ProductReview review = productReviewRepo.findById(reviewId)
				.orElseThrow(() -> new ResourceNotFoundException("ProductReview", "reviewId", reviewId));

		if (!review.getProduct().getProductId().equals(productId)) {
			throw new APIException("Review does not belong to the specified product");
		}

		if (!review.getUser().getUserId().equals(user.getUserId())) {
			throw new APIException("You can only update your own review");
		}

		review.setRating(reviewDTO.getRating());
		review.setComment(reviewDTO.getComment());
		review.setUpdatedAt(LocalDateTime.now());

		ProductReview updatedReview = productReviewRepo.save(review);
		return mapToDTO(updatedReview);
	}

	@Override
	public String deleteReview(String email, Long productId, Long reviewId) {
		User user = getUserByEmail(email);
		getProductById(productId);

		ProductReview review = productReviewRepo.findById(reviewId)
				.orElseThrow(() -> new ResourceNotFoundException("ProductReview", "reviewId", reviewId));

		if (!review.getProduct().getProductId().equals(productId)) {
			throw new APIException("Review does not belong to the specified product");
		}

		if (!review.getUser().getUserId().equals(user.getUserId())) {
			throw new APIException("You can only delete your own review");
		}

		productReviewRepo.delete(review);
		return "Review deleted successfully";
	}
}