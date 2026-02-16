package com.app.payloads;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// === VAR-3: Store Discount (Diskon Toko) ===
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreDiscountDTO {

	private Long discountId;
	private String discountName;
	private Double discountPercentage;
	private Double minOrderAmount;
	private Boolean active;
	private LocalDate startDate;
	private LocalDate endDate;
}
