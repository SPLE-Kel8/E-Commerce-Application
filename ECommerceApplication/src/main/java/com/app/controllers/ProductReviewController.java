package com.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.payloads.CreateProductReviewDTO;
import com.app.payloads.ProductReviewDTO;
import com.app.payloads.ProductReviewResponse;
import com.app.security.AuthUtil;
import com.app.services.ProductReviewService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "E-Commerce Application")
public class ProductReviewController {

	@Autowired
	private ProductReviewService productReviewService;

	@Autowired
	private AuthUtil authUtil;

	@GetMapping("/public/products/{productId}/reviews")
	public ResponseEntity<ProductReviewResponse> getReviewsByProduct(
			@PathVariable Long productId,
			@RequestParam(name = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
			@RequestParam(name = "pageSize", defaultValue = "5", required = false) Integer pageSize,
			@RequestParam(name = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
			@RequestParam(name = "sortOrder", defaultValue = "desc", required = false) String sortOrder) {

		ProductReviewResponse response = productReviewService.getReviewsByProduct(productId, pageNumber, pageSize, sortBy,
				sortOrder);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/public/users/{email}/products/{productId}/reviews/me")
	public ResponseEntity<ProductReviewDTO> getMyReviewForProduct(@PathVariable String email, @PathVariable Long productId) {
		authUtil.validateUserAccess(email);

		ProductReviewDTO review = productReviewService.getReviewByUserAndProduct(email, productId);
		return new ResponseEntity<>(review, HttpStatus.OK);
	}

	@PostMapping("/public/users/{email}/products/{productId}/reviews")
	public ResponseEntity<ProductReviewDTO> createReview(@PathVariable String email, @PathVariable Long productId,
			@Valid @RequestBody CreateProductReviewDTO reviewDTO) {
		authUtil.validateUserAccess(email);

		ProductReviewDTO savedReview = productReviewService.createReview(email, productId, reviewDTO);
		return new ResponseEntity<>(savedReview, HttpStatus.CREATED);
	}

	@PutMapping("/public/users/{email}/products/{productId}/reviews/{reviewId}")
	public ResponseEntity<ProductReviewDTO> updateReview(@PathVariable String email, @PathVariable Long productId,
			@PathVariable Long reviewId, @Valid @RequestBody CreateProductReviewDTO reviewDTO) {
		authUtil.validateUserAccess(email);

		ProductReviewDTO updatedReview = productReviewService.updateReview(email, productId, reviewId, reviewDTO);
		return new ResponseEntity<>(updatedReview, HttpStatus.OK);
	}

	@DeleteMapping("/public/users/{email}/products/{productId}/reviews/{reviewId}")
	public ResponseEntity<String> deleteReview(@PathVariable String email, @PathVariable Long productId,
			@PathVariable Long reviewId) {
		authUtil.validateUserAccess(email);

		String message = productReviewService.deleteReview(email, productId, reviewId);
		return new ResponseEntity<>(message, HttpStatus.OK);
	}
}