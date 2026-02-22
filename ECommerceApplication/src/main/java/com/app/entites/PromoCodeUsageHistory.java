package com.app.entites;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "promo_code_usage_history")
@AllArgsConstructor
@NoArgsConstructor
public class PromoCodeUsageHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long historyId;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "promo_code_id", nullable = false)
	private PromoCode promoCode;

	@ManyToOne
	@JoinColumn(name = "order_id", nullable = true)
	private Order order;

	@Column(name = "promo_code_used")
	private String promoCodeUsed;

	@Column(name = "discount_amount")
	private Double discountAmount;

	@Column(name = "order_amount")
	private Double orderAmount;

	@Column(name = "final_amount")
	private Double finalAmount;

	@Column(name = "used_at")
	private LocalDateTime usedAt;

	@Column(name = "status")
	private String status; // APPLIED, EXPIRED, CANCELLED

}
