package com.app.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.app.entites.ProductReview;

@Repository
public interface ProductReviewRepo extends JpaRepository<ProductReview, Long> {

	Page<ProductReview> findByProductProductId(Long productId, Pageable pageable);

	Optional<ProductReview> findByProductProductIdAndUserUserId(Long productId, Long userId);

	boolean existsByProductProductIdAndUserUserId(Long productId, Long userId);

	long countByProductProductId(Long productId);

	@Query("SELECT AVG(pr.rating) FROM ProductReview pr WHERE pr.product.productId = :productId")
	Double findAverageRatingByProductId(Long productId);
}