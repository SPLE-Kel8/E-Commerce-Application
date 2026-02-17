package com.app.services;

import java.util.List;

import com.app.payloads.PromoCodeDTO;

public interface PromoCodeService {

	PromoCodeDTO createPromoCode(PromoCodeDTO promoCodeDTO);

	PromoCodeDTO getPromoCodeById(Long promoCodeId);

	PromoCodeDTO getPromoCodeByCode(String code);

	List<PromoCodeDTO> getAllPromoCodes();

	PromoCodeDTO updatePromoCode(Long promoCodeId, PromoCodeDTO promoCodeDTO);

	String deletePromoCode(Long promoCodeId);

	PromoCodeDTO validatePromoCode(String code, Double orderAmount);

	Double calculateDiscount(String code, Double orderAmount);
}
