package com.app.payloads;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cart DTO.
 * 
 * Requirement (f): When promo code is applied, product discount is NOT counted.
 * originalTotalPrice is used for promo code calculation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
	
	private Long cartId;
	
	/** Total price with product discount (specialPrice) */
	private Double totalPrice = 0.0;
	
	/** Total price without product discount (original price) - for promo code calculation */
	private Double originalTotalPrice = 0.0;
	
	private List<ProductDTO> products = new ArrayList<>();
	private String appliedPromoCode;
	private Double discountAmount = 0.0;
	
	/** Final price - when promo applied: originalTotalPrice - discountAmount; otherwise: totalPrice */
	private Double finalPrice = 0.0;
}
