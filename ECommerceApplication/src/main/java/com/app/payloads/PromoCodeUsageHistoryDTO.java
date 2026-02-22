package com.app.payloads;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromoCodeUsageHistoryDTO {

	private Long historyId;

	private Long userId;

	private String userEmail;

	private Long promoCodeId;

	private String promoCodeUsed;

	private String promoCodeDescription;

	private Double discountAmount;

	private Double orderAmount;

	private Double finalAmount;

	private LocalDateTime usedAt;

	private String status;

	private Long orderId;

}
