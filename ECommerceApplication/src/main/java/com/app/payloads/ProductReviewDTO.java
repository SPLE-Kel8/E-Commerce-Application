package com.app.payloads;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewDTO {

	private Long reviewId;
	private Long productId;
	private Long userId;
	private String userEmail;
	private Integer rating;
	private String comment;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}