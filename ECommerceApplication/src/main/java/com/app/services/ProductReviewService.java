package com.app.services;

import com.app.payloads.CreateProductReviewDTO;
import com.app.payloads.ProductReviewDTO;
import com.app.payloads.ProductReviewResponse;

public interface ProductReviewService {

	ProductReviewDTO createReview(String email, Long productId, CreateProductReviewDTO reviewDTO);

	ProductReviewResponse getReviewsByProduct(Long productId, Integer pageNumber, Integer pageSize, String sortBy,
			String sortOrder);

	ProductReviewDTO getReviewByUserAndProduct(String email, Long productId);

	ProductReviewDTO updateReview(String email, Long productId, Long reviewId, CreateProductReviewDTO reviewDTO);

	String deleteReview(String email, Long productId, Long reviewId);
}