package com.app.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
	private Long paymentId;
	private String paymentMethod;

	// === VAR-3: Credit Card Payment ===
	private String maskedCardNumber;
	private String cardExpiry;

}
