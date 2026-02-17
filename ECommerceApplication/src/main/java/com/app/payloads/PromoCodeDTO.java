package com.app.payloads;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeDTO {
	
	private Long promoCodeId;
	private String code;
	private String description;
	private Double discountPercentage;
	private LocalDate startDate;
	private LocalDate endDate;
	private Boolean isActive;
	private Double minimumOrderAmount;
	private Integer usageLimit;
	private Integer usedCount;
}
