package com.app.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.entites.PromoCodeUsageHistory;

@Repository
public interface PromoCodeUsageHistoryRepo extends JpaRepository<PromoCodeUsageHistory, Long> {

	// Get history by user using JPQL query
	@Query("SELECT p FROM PromoCodeUsageHistory p WHERE p.user.userId = :userId")
	List<PromoCodeUsageHistory> findByUserId(@Param("userId") Long userId);

	// Get history by promo code using JPQL query
	@Query("SELECT p FROM PromoCodeUsageHistory p WHERE p.promoCode.promoCodeId = :promoCodeId")
	List<PromoCodeUsageHistory> findByPromoCodeId(@Param("promoCodeId") Long promoCodeId);

	// Get history by order
	@Query("SELECT p FROM PromoCodeUsageHistory p WHERE p.order.orderId = :orderId")
	PromoCodeUsageHistory findByOrderId(@Param("orderId") Long orderId);

	// Count usage by promo code
	@Query("SELECT COUNT(p) FROM PromoCodeUsageHistory p WHERE p.promoCode.promoCodeId = :promoCodeId")
	long countByPromoCodeId(@Param("promoCodeId") Long promoCodeId);

	// Count usage by promo code and user
	@Query("SELECT COUNT(p) FROM PromoCodeUsageHistory p WHERE p.promoCode.promoCodeId = :promoCodeId AND p.user.userId = :userId")
	long countByPromoCodeIdAndUserId(@Param("promoCodeId") Long promoCodeId, @Param("userId") Long userId);

}
