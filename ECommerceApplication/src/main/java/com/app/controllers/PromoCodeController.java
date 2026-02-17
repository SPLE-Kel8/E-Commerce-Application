package com.app.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.config.FeatureConfig;
import com.app.exceptions.APIException;
import com.app.payloads.PromoCodeDTO;
import com.app.services.PromoCodeService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

/**
 * Promo Code Controller - manages discount promo codes.
 * 
 * Clone-and-Own Notes:
 * - This feature can be disabled via app.feature.promo-code.enabled=false
 * - When disabled, all endpoints return 400 Bad Request
 * - To customize promo code behavior, modify PromoCodeServiceImpl
 */
@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "E-Commerce Application")
public class PromoCodeController {

	@Autowired
	private PromoCodeService promoCodeService;

	@Autowired
	private FeatureConfig featureConfig;

	private void checkPromoCodeFeatureEnabled() {
		if (!featureConfig.isPromoCodeEnabled()) {
			throw new APIException("Promo code feature is not enabled for this store");
		}
	}

	@PostMapping("/admin/promocodes")
	public ResponseEntity<PromoCodeDTO> createPromoCode(@Valid @RequestBody PromoCodeDTO promoCodeDTO) {
		checkPromoCodeFeatureEnabled();
		PromoCodeDTO createdPromoCode = promoCodeService.createPromoCode(promoCodeDTO);
		return new ResponseEntity<>(createdPromoCode, HttpStatus.CREATED);
	}

	@GetMapping("/admin/promocodes")
	public ResponseEntity<List<PromoCodeDTO>> getAllPromoCodes() {
		checkPromoCodeFeatureEnabled();
		List<PromoCodeDTO> promoCodes = promoCodeService.getAllPromoCodes();
		return new ResponseEntity<>(promoCodes, HttpStatus.OK);
	}

	@GetMapping("/admin/promocodes/{promoCodeId}")
	public ResponseEntity<PromoCodeDTO> getPromoCodeById(@PathVariable Long promoCodeId) {
		checkPromoCodeFeatureEnabled();
		PromoCodeDTO promoCode = promoCodeService.getPromoCodeById(promoCodeId);
		return new ResponseEntity<>(promoCode, HttpStatus.OK);
	}

	@GetMapping("/public/promocodes/validate")
	public ResponseEntity<PromoCodeDTO> validatePromoCode(
			@RequestParam String code,
			@RequestParam Double orderAmount) {
		checkPromoCodeFeatureEnabled();
		PromoCodeDTO promoCode = promoCodeService.validatePromoCode(code, orderAmount);
		return new ResponseEntity<>(promoCode, HttpStatus.OK);
	}

	@GetMapping("/public/promocodes/calculate-discount")
	public ResponseEntity<Double> calculateDiscount(
			@RequestParam String code,
			@RequestParam Double orderAmount) {
		checkPromoCodeFeatureEnabled();
		Double discount = promoCodeService.calculateDiscount(code, orderAmount);
		return new ResponseEntity<>(discount, HttpStatus.OK);
	}

	@PutMapping("/admin/promocodes/{promoCodeId}")
	public ResponseEntity<PromoCodeDTO> updatePromoCode(
			@PathVariable Long promoCodeId,
			@Valid @RequestBody PromoCodeDTO promoCodeDTO) {
		checkPromoCodeFeatureEnabled();
		PromoCodeDTO updatedPromoCode = promoCodeService.updatePromoCode(promoCodeId, promoCodeDTO);
		return new ResponseEntity<>(updatedPromoCode, HttpStatus.OK);
	}

	@DeleteMapping("/admin/promocodes/{promoCodeId}")
	public ResponseEntity<String> deletePromoCode(@PathVariable Long promoCodeId) {
		checkPromoCodeFeatureEnabled();
		String status = promoCodeService.deletePromoCode(promoCodeId);
		return new ResponseEntity<>(status, HttpStatus.OK);
	}
}
