package com.app.entites;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "promo_codes")
@AllArgsConstructor
@NoArgsConstructor
public class PromoCode {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long promoCodeId;

	@NotBlank
	@Size(min = 3, max = 20, message = "Promo code must be between 3 and 20 characters")
	@Column(unique = true)
	private String code;

	@NotBlank
	@Size(min = 5, message = "Description must contain at least 5 characters")
	private String description;

	@Min(value = 1, message = "Discount percentage must be at least 1")
	@Max(value = 100, message = "Discount percentage cannot exceed 100")
	private Double discountPercentage;

	private LocalDate startDate;

	private LocalDate endDate;

	private Boolean isActive = true;

	@Min(value = 0, message = "Minimum order amount cannot be negative")
	private Double minimumOrderAmount = 0.0;

	private Integer usageLimit;

	private Integer usedCount = 0;
}
