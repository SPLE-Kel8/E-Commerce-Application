package com.app.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.app.entites.StoreDiscount;

// === VAR-3: Store Discount (Diskon Toko) ===
@Repository
public interface StoreDiscountRepo extends JpaRepository<StoreDiscount, Long> {

	@Query("SELECT sd FROM StoreDiscount sd WHERE sd.active = true AND sd.startDate <= :today AND sd.endDate >= :today AND sd.minOrderAmount <= :orderAmount ORDER BY sd.discountPercentage DESC")
	List<StoreDiscount> findApplicableDiscounts(LocalDate today, Double orderAmount);

	@Query("SELECT sd FROM StoreDiscount sd WHERE sd.active = true AND sd.startDate <= :today AND sd.endDate >= :today ORDER BY sd.discountPercentage DESC")
	List<StoreDiscount> findActiveDiscounts(LocalDate today);
}
