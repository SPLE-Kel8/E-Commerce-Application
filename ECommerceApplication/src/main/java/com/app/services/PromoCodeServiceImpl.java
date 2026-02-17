package com.app.services;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.entites.PromoCode;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.PromoCodeDTO;
import com.app.repositories.PromoCodeRepo;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class PromoCodeServiceImpl implements PromoCodeService {

	@Autowired
	private PromoCodeRepo promoCodeRepo;

	@Autowired
	private ModelMapper modelMapper;

	@Override
	public PromoCodeDTO createPromoCode(PromoCodeDTO promoCodeDTO) {
		if (promoCodeRepo.existsByCode(promoCodeDTO.getCode())) {
			throw new APIException("Promo code '" + promoCodeDTO.getCode() + "' already exists");
		}

		PromoCode promoCode = modelMapper.map(promoCodeDTO, PromoCode.class);
		promoCode.setUsedCount(0);
		promoCode.setIsActive(true);

		PromoCode savedPromoCode = promoCodeRepo.save(promoCode);
		return modelMapper.map(savedPromoCode, PromoCodeDTO.class);
	}

	@Override
	public PromoCodeDTO getPromoCodeById(Long promoCodeId) {
		PromoCode promoCode = promoCodeRepo.findById(promoCodeId)
				.orElseThrow(() -> new ResourceNotFoundException("PromoCode", "promoCodeId", promoCodeId));
		return modelMapper.map(promoCode, PromoCodeDTO.class);
	}

	@Override
	public PromoCodeDTO getPromoCodeByCode(String code) {
		PromoCode promoCode = promoCodeRepo.findByCode(code)
				.orElseThrow(() -> new ResourceNotFoundException("PromoCode", "code", code));
		return modelMapper.map(promoCode, PromoCodeDTO.class);
	}

	@Override
	public List<PromoCodeDTO> getAllPromoCodes() {
		List<PromoCode> promoCodes = promoCodeRepo.findAll();
		if (promoCodes.isEmpty()) {
			throw new APIException("No promo codes found");
		}
		return promoCodes.stream()
				.map(pc -> modelMapper.map(pc, PromoCodeDTO.class))
				.collect(Collectors.toList());
	}

	@Override
	public PromoCodeDTO updatePromoCode(Long promoCodeId, PromoCodeDTO promoCodeDTO) {
		PromoCode existingPromoCode = promoCodeRepo.findById(promoCodeId)
				.orElseThrow(() -> new ResourceNotFoundException("PromoCode", "promoCodeId", promoCodeId));

		if (!existingPromoCode.getCode().equals(promoCodeDTO.getCode()) && 
				promoCodeRepo.existsByCode(promoCodeDTO.getCode())) {
			throw new APIException("Promo code '" + promoCodeDTO.getCode() + "' already exists");
		}

		existingPromoCode.setCode(promoCodeDTO.getCode());
		existingPromoCode.setDescription(promoCodeDTO.getDescription());
		existingPromoCode.setDiscountPercentage(promoCodeDTO.getDiscountPercentage());
		existingPromoCode.setStartDate(promoCodeDTO.getStartDate());
		existingPromoCode.setEndDate(promoCodeDTO.getEndDate());
		existingPromoCode.setIsActive(promoCodeDTO.getIsActive());
		existingPromoCode.setMinimumOrderAmount(promoCodeDTO.getMinimumOrderAmount());
		existingPromoCode.setUsageLimit(promoCodeDTO.getUsageLimit());

		PromoCode updatedPromoCode = promoCodeRepo.save(existingPromoCode);
		return modelMapper.map(updatedPromoCode, PromoCodeDTO.class);
	}

	@Override
	public String deletePromoCode(Long promoCodeId) {
		PromoCode promoCode = promoCodeRepo.findById(promoCodeId)
				.orElseThrow(() -> new ResourceNotFoundException("PromoCode", "promoCodeId", promoCodeId));
		promoCodeRepo.delete(promoCode);
		return "Promo code '" + promoCode.getCode() + "' deleted successfully";
	}

	@Override
	public PromoCodeDTO validatePromoCode(String code, Double orderAmount) {
		PromoCode promoCode = promoCodeRepo.findByCode(code)
				.orElseThrow(() -> new APIException("Invalid promo code: " + code));

		LocalDate today = LocalDate.now();

		if (!promoCode.getIsActive()) {
			throw new APIException("Promo code is not active");
		}

		if (promoCode.getStartDate() != null && today.isBefore(promoCode.getStartDate())) {
			throw new APIException("Promo code is not yet valid");
		}

		if (promoCode.getEndDate() != null && today.isAfter(promoCode.getEndDate())) {
			throw new APIException("Promo code has expired");
		}

		if (promoCode.getUsageLimit() != null && promoCode.getUsedCount() >= promoCode.getUsageLimit()) {
			throw new APIException("Promo code usage limit reached");
		}

		if (promoCode.getMinimumOrderAmount() != null && orderAmount < promoCode.getMinimumOrderAmount()) {
			throw new APIException("Minimum order amount of " + promoCode.getMinimumOrderAmount() + " is required for this promo code");
		}

		return modelMapper.map(promoCode, PromoCodeDTO.class);
	}

	@Override
	public Double calculateDiscount(String code, Double orderAmount) {
		validatePromoCode(code, orderAmount);
		
		PromoCode promoCode = promoCodeRepo.findByCode(code)
				.orElseThrow(() -> new APIException("Invalid promo code: " + code));
		
		Double discountAmount = orderAmount * (promoCode.getDiscountPercentage() / 100);
		return discountAmount;
	}
}
