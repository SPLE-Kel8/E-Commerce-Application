package com.app.services;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.entites.PromoCodeUsageHistory;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.PromoCodeUsageHistoryDTO;
import com.app.repositories.PromoCodeUsageHistoryRepo;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class PromoCodeUsageHistoryServiceImpl implements PromoCodeUsageHistoryService {

	@Autowired
	private PromoCodeUsageHistoryRepo historyRepo;

	@Autowired
	private ModelMapper modelMapper;

	@Override
	public PromoCodeUsageHistoryDTO recordPromoCodeUsage(PromoCodeUsageHistoryDTO historyDTO) {
		PromoCodeUsageHistory history = modelMapper.map(historyDTO, PromoCodeUsageHistory.class);
		PromoCodeUsageHistory savedHistory = historyRepo.save(history);
		return modelMapper.map(savedHistory, PromoCodeUsageHistoryDTO.class);
	}

	@Override
	public List<PromoCodeUsageHistoryDTO> getUserPromoCodeHistory(Long userId) {
		List<PromoCodeUsageHistory> histories = historyRepo.findByUserId(userId);
		if (histories.isEmpty()) {
			throw new ResourceNotFoundException("PromoCode Usage History", "userId", userId);
		}
		return histories.stream()
				.map(this::mapToDTO)
				.collect(Collectors.toList());
	}

	@Override
	public List<PromoCodeUsageHistoryDTO> getAllPromoCodeHistory() {
		List<PromoCodeUsageHistory> histories = historyRepo.findAll();
		if (histories.isEmpty()) {
			throw new ResourceNotFoundException("PromoCode Usage History", "all", "empty");
		}
		return histories.stream()
				.map(this::mapToDTO)
				.collect(Collectors.toList());
	}

	@Override
	public List<PromoCodeUsageHistoryDTO> getHistoryByPromoCode(Long promoCodeId) {
		List<PromoCodeUsageHistory> histories = historyRepo.findByPromoCodeId(promoCodeId);
		if (histories.isEmpty()) {
			throw new ResourceNotFoundException("PromoCode Usage History", "promoCodeId", promoCodeId);
		}
		return histories.stream()
				.map(this::mapToDTO)
				.collect(Collectors.toList());
	}

	@Override
	public List<PromoCodeUsageHistoryDTO> getHistoryByUserAndPromoCode(Long userId, Long promoCodeId) {
		List<PromoCodeUsageHistory> histories = historyRepo.findByUserId(userId);
		List<PromoCodeUsageHistory> filtered = histories.stream()
				.filter(h -> h.getPromoCode().getPromoCodeId().equals(promoCodeId))
				.collect(Collectors.toList());
		
		if (filtered.isEmpty()) {
			throw new ResourceNotFoundException("PromoCode Usage History", "userId and promoCodeId", 
					userId + " and " + promoCodeId);
		}
		return filtered.stream()
				.map(this::mapToDTO)
				.collect(Collectors.toList());
	}

	@Override
	public PromoCodeUsageHistoryDTO getHistoryById(Long historyId) {
		PromoCodeUsageHistory history = historyRepo.findById(historyId)
				.orElseThrow(() -> new ResourceNotFoundException("PromoCode Usage History", "historyId", historyId));
		return mapToDTO(history);
	}

	@Override
	public long getPromoCodeUsageCount(Long promoCodeId) {
		return historyRepo.countByPromoCodeId(promoCodeId);
	}

	@Override
	public long getUserPromoCodeUsageCount(Long promoCodeId, Long userId) {
		return historyRepo.countByPromoCodeIdAndUserId(promoCodeId, userId);
	}

	private PromoCodeUsageHistoryDTO mapToDTO(PromoCodeUsageHistory history) {
		PromoCodeUsageHistoryDTO dto = new PromoCodeUsageHistoryDTO();
		dto.setHistoryId(history.getHistoryId());
		dto.setUserId(history.getUser().getUserId());
		dto.setUserEmail(history.getUser().getEmail());
		dto.setPromoCodeId(history.getPromoCode().getPromoCodeId());
		dto.setPromoCodeUsed(history.getPromoCodeUsed());
		dto.setPromoCodeDescription(history.getPromoCode().getDescription());
		dto.setDiscountAmount(history.getDiscountAmount());
		dto.setOrderAmount(history.getOrderAmount());
		dto.setFinalAmount(history.getFinalAmount());
		dto.setUsedAt(history.getUsedAt());
		dto.setStatus(history.getStatus());
		if (history.getOrder() != null) {
			dto.setOrderId(history.getOrder().getOrderId());
		}
		return dto;
	}

}
