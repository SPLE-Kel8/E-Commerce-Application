package com.app.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.config.FeatureConfig;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.PromoCodeUsageHistoryDTO;
import com.app.repositories.UserRepo;
import com.app.services.PromoCodeUsageHistoryService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * Promo Code Usage History Controller - manages history of promo code usage.
 * 
 * Features:
 * - Users can only view their own promo code usage history
 * - Admins can view history of all users
 * - History records are read-only (cannot be edited or deleted)
 */
@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "E-Commerce Application")
public class PromoCodeUsageHistoryController {

	@Autowired
	private PromoCodeUsageHistoryService historyService;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private FeatureConfig featureConfig;

	private void checkPromoCodeFeatureEnabled() {
		if (!featureConfig.isPromoCodeEnabled()) {
			throw new APIException("Promo code feature is not enabled for this store");
		}
	}

	private String getCurrentUserEmail() {
		return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private Long getCurrentUserId() {
		String email = getCurrentUserEmail();
		return userRepo.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User", "email", email))
				.getUserId();
	}

	private boolean isUserAdmin() {
		return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
				.stream()
				.anyMatch(auth -> auth.getAuthority().equals("ADMIN"));
	}

	/**
	 * Get promo code usage history for current user
	 * Users can only view their own history
	 * 
	 * @return List of promo code usage history
	 */
	@GetMapping("/public/promo-code-usage-history")
	public ResponseEntity<List<PromoCodeUsageHistoryDTO>> getMyPromoCodeHistory() {
		checkPromoCodeFeatureEnabled();
		Long userId = getCurrentUserId();
		List<PromoCodeUsageHistoryDTO> history = historyService.getUserPromoCodeHistory(userId);
		return new ResponseEntity<>(history, HttpStatus.OK);
	}

	/**
	 * Get promo code usage history for specific user
	 * Only admin can access this endpoint
	 * 
	 * @param userId
	 * @return List of promo code usage history for specific user
	 */
	@GetMapping("/admin/promo-code-usage-history/user/{userId}")
	public ResponseEntity<List<PromoCodeUsageHistoryDTO>> getUserPromoCodeHistory(@PathVariable Long userId) {
		checkPromoCodeFeatureEnabled();
		
		// Optionally verify the user exists
		userRepo.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
		
		List<PromoCodeUsageHistoryDTO> history = historyService.getUserPromoCodeHistory(userId);
		return new ResponseEntity<>(history, HttpStatus.OK);
	}

	/**
	 * Get all promo code usage history
	 * Only admin can access this endpoint
	 * 
	 * @return List of all promo code usage history
	 */
	@GetMapping("/admin/promo-code-usage-history")
	public ResponseEntity<List<PromoCodeUsageHistoryDTO>> getAllPromoCodeHistory() {
		checkPromoCodeFeatureEnabled();
		List<PromoCodeUsageHistoryDTO> history = historyService.getAllPromoCodeHistory();
		return new ResponseEntity<>(history, HttpStatus.OK);
	}

	/**
	 * Get promo code usage history for specific promo code
	 * Only admin can access this endpoint
	 * 
	 * @param promoCodeId
	 * @return List of promo code usage history for specific promo code
	 */
	@GetMapping("/admin/promo-code-usage-history/promo-code/{promoCodeId}")
	public ResponseEntity<List<PromoCodeUsageHistoryDTO>> getHistoryByPromoCode(@PathVariable Long promoCodeId) {
		checkPromoCodeFeatureEnabled();
		List<PromoCodeUsageHistoryDTO> history = historyService.getHistoryByPromoCode(promoCodeId);
		return new ResponseEntity<>(history, HttpStatus.OK);
	}

	/**
	 * Get promo code usage history for specific user and promo code
	 * Only admin can access this endpoint
	 * 
	 * @param userId
	 * @param promoCodeId
	 * @return List of promo code usage history
	 */
	@GetMapping("/admin/promo-code-usage-history/user/{userId}/promo-code/{promoCodeId}")
	public ResponseEntity<List<PromoCodeUsageHistoryDTO>> getHistoryByUserAndPromoCode(
			@PathVariable Long userId,
			@PathVariable Long promoCodeId) {
		checkPromoCodeFeatureEnabled();
		List<PromoCodeUsageHistoryDTO> history = historyService.getHistoryByUserAndPromoCode(userId, promoCodeId);
		return new ResponseEntity<>(history, HttpStatus.OK);
	}

	/**
	 * Get specific history detail
	 * Users can only view their own history
	 * Admins can view any history
	 * 
	 * @param historyId
	 * @return Promo code usage history detail
	 */
	@GetMapping("/public/promo-code-usage-history/{historyId}")
	public ResponseEntity<PromoCodeUsageHistoryDTO> getHistoryById(@PathVariable Long historyId) {
		checkPromoCodeFeatureEnabled();
		
		PromoCodeUsageHistoryDTO history = historyService.getHistoryById(historyId);
		
		// Check if user is authorized to view this history
		Long currentUserId = getCurrentUserId();
		if (!isUserAdmin() && !history.getUserId().equals(currentUserId)) {
			throw new APIException("You are not authorized to view this history");
		}
		
		return new ResponseEntity<>(history, HttpStatus.OK);
	}

}
