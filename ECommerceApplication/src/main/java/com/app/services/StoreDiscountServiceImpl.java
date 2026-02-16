package com.app.services;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.entites.StoreDiscount;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.StoreDiscountDTO;
import com.app.repositories.StoreDiscountRepo;

import jakarta.transaction.Transactional;

// === VAR-3: Store Discount (Diskon Toko) ===
@Transactional
@Service
public class StoreDiscountServiceImpl implements StoreDiscountService {

	@Autowired
	private StoreDiscountRepo storeDiscountRepo;

	@Autowired
	private ModelMapper modelMapper;

	@Override
	public StoreDiscountDTO createDiscount(StoreDiscountDTO storeDiscountDTO) {
		StoreDiscount storeDiscount = modelMapper.map(storeDiscountDTO, StoreDiscount.class);

		if (storeDiscount.getStartDate() == null) {
			storeDiscount.setStartDate(LocalDate.now());
		}
		if (storeDiscount.getEndDate() == null) {
			storeDiscount.setEndDate(LocalDate.now().plusMonths(1));
		}
		if (storeDiscount.getEndDate().isBefore(storeDiscount.getStartDate())) {
			throw new APIException("End date must be after start date");
		}

		StoreDiscount saved = storeDiscountRepo.save(storeDiscount);
		return modelMapper.map(saved, StoreDiscountDTO.class);
	}

	@Override
	public StoreDiscountDTO updateDiscount(Long discountId, StoreDiscountDTO storeDiscountDTO) {
		StoreDiscount existing = storeDiscountRepo.findById(discountId)
				.orElseThrow(() -> new ResourceNotFoundException("StoreDiscount", "discountId", discountId));

		existing.setDiscountName(storeDiscountDTO.getDiscountName());
		existing.setDiscountPercentage(storeDiscountDTO.getDiscountPercentage());
		existing.setMinOrderAmount(storeDiscountDTO.getMinOrderAmount());
		existing.setActive(storeDiscountDTO.getActive());
		existing.setStartDate(storeDiscountDTO.getStartDate());
		existing.setEndDate(storeDiscountDTO.getEndDate());

		return modelMapper.map(existing, StoreDiscountDTO.class);
	}

	@Override
	public StoreDiscountDTO getDiscount(Long discountId) {
		StoreDiscount storeDiscount = storeDiscountRepo.findById(discountId)
				.orElseThrow(() -> new ResourceNotFoundException("StoreDiscount", "discountId", discountId));
		return modelMapper.map(storeDiscount, StoreDiscountDTO.class);
	}

	@Override
	public List<StoreDiscountDTO> getAllDiscounts() {
		List<StoreDiscount> discounts = storeDiscountRepo.findAll();
		if (discounts.isEmpty()) {
			throw new APIException("No store discounts found");
		}
		return discounts.stream()
				.map(d -> modelMapper.map(d, StoreDiscountDTO.class))
				.collect(Collectors.toList());
	}

	@Override
	public List<StoreDiscountDTO> getActiveDiscounts() {
		List<StoreDiscount> discounts = storeDiscountRepo
				.findActiveDiscounts(LocalDate.now());
		if (discounts.isEmpty()) {
			throw new APIException("No active store discounts found");
		}
		return discounts.stream()
				.map(d -> modelMapper.map(d, StoreDiscountDTO.class))
				.collect(Collectors.toList());
	}

	@Override
	public String deleteDiscount(Long discountId) {
		StoreDiscount storeDiscount = storeDiscountRepo.findById(discountId)
				.orElseThrow(() -> new ResourceNotFoundException("StoreDiscount", "discountId", discountId));
		storeDiscountRepo.delete(storeDiscount);
		return "Store discount '" + storeDiscount.getDiscountName() + "' deleted successfully!";
	}
}
