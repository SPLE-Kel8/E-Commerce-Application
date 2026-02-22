package com.app.services;

import java.util.List;

import com.app.payloads.PromoCodeUsageHistoryDTO;

public interface PromoCodeUsageHistoryService {

	// Create history record
	PromoCodeUsageHistoryDTO recordPromoCodeUsage(PromoCodeUsageHistoryDTO historyDTO);

	// Get history by user (untuk user lihat history sendiri)
	List<PromoCodeUsageHistoryDTO> getUserPromoCodeHistory(Long userId);

	// Get all history (untuk admin lihat semua history)
	List<PromoCodeUsageHistoryDTO> getAllPromoCodeHistory();

	// Get history by promo code (untuk admin)
	List<PromoCodeUsageHistoryDTO> getHistoryByPromoCode(Long promoCodeId);

	// Get history by specific user and promo code (untuk admin)
	List<PromoCodeUsageHistoryDTO> getHistoryByUserAndPromoCode(Long userId, Long promoCodeId);

	// Get detail history
	PromoCodeUsageHistoryDTO getHistoryById(Long historyId);

	// Get usage count for specific promo code
	long getPromoCodeUsageCount(Long promoCodeId);

	// Get usage count for specific user and promo code
	long getUserPromoCodeUsageCount(Long promoCodeId, Long userId);

}
