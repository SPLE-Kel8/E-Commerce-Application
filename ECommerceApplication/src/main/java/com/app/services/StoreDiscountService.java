package com.app.services;

import java.util.List;

import com.app.payloads.StoreDiscountDTO;

// === VAR-3: Store Discount (Diskon Toko) ===
public interface StoreDiscountService {

	StoreDiscountDTO createDiscount(StoreDiscountDTO storeDiscountDTO);

	StoreDiscountDTO updateDiscount(Long discountId, StoreDiscountDTO storeDiscountDTO);

	StoreDiscountDTO getDiscount(Long discountId);

	List<StoreDiscountDTO> getAllDiscounts();

	List<StoreDiscountDTO> getActiveDiscounts();

	String deleteDiscount(Long discountId);
}
