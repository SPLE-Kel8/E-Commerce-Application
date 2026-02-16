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
import org.springframework.web.bind.annotation.RestController;

import com.app.payloads.StoreDiscountDTO;
import com.app.services.StoreDiscountService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

// === VAR-3: Store Discount (Diskon Toko) ===
@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "E-Commerce Application")
public class StoreDiscountController {

	@Autowired
	private StoreDiscountService storeDiscountService;

	// Admin: Create a store discount
	@PostMapping("/admin/store-discounts")
	public ResponseEntity<StoreDiscountDTO> createDiscount(@Valid @RequestBody StoreDiscountDTO storeDiscountDTO) {
		StoreDiscountDTO created = storeDiscountService.createDiscount(storeDiscountDTO);
		return new ResponseEntity<>(created, HttpStatus.CREATED);
	}

	// Admin: Update a store discount
	@PutMapping("/admin/store-discounts/{discountId}")
	public ResponseEntity<StoreDiscountDTO> updateDiscount(@PathVariable Long discountId,
			@Valid @RequestBody StoreDiscountDTO storeDiscountDTO) {
		StoreDiscountDTO updated = storeDiscountService.updateDiscount(discountId, storeDiscountDTO);
		return new ResponseEntity<>(updated, HttpStatus.OK);
	}

	// Admin: Delete a store discount
	@DeleteMapping("/admin/store-discounts/{discountId}")
	public ResponseEntity<String> deleteDiscount(@PathVariable Long discountId) {
		String message = storeDiscountService.deleteDiscount(discountId);
		return new ResponseEntity<>(message, HttpStatus.OK);
	}

	// Admin: Get all store discounts
	@GetMapping("/admin/store-discounts")
	public ResponseEntity<List<StoreDiscountDTO>> getAllDiscounts() {
		List<StoreDiscountDTO> discounts = storeDiscountService.getAllDiscounts();
		return new ResponseEntity<>(discounts, HttpStatus.OK);
	}

	// Public: Get active store discounts (users can see what discounts are available)
	@GetMapping("/public/store-discounts/active")
	public ResponseEntity<List<StoreDiscountDTO>> getActiveDiscounts() {
		List<StoreDiscountDTO> discounts = storeDiscountService.getActiveDiscounts();
		return new ResponseEntity<>(discounts, HttpStatus.OK);
	}

	// Admin: Get a specific store discount
	@GetMapping("/admin/store-discounts/{discountId}")
	public ResponseEntity<StoreDiscountDTO> getDiscount(@PathVariable Long discountId) {
		StoreDiscountDTO discount = storeDiscountService.getDiscount(discountId);
		return new ResponseEntity<>(discount, HttpStatus.OK);
	}
}
