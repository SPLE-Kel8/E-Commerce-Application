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

// === VAR-3: Store Discount (Diskon Toko) ===
@Entity
@Data
@Table(name = "store_discounts")
@NoArgsConstructor
@AllArgsConstructor
public class StoreDiscount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long discountId;

	@NotBlank
	@Size(min = 3, message = "Discount name must contain at least 3 characters")
	@Column(unique = true)
	private String discountName;

	@Min(value = 1, message = "Discount percentage must be at least 1%")
	@Max(value = 50, message = "Discount percentage cannot exceed 50%")
	private Double discountPercentage;

	private Double minOrderAmount = 0.0; // minimum order amount to qualify

	private Boolean active = true;

	private LocalDate startDate;

	private LocalDate endDate;
}
