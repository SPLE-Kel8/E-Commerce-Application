package com.app.payloads;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// === VAR-3: Credit Card Payment ===
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditCardPaymentRequest {

	@NotBlank(message = "Card number is required")
	@Pattern(regexp = "\\d{16}", message = "Card number must be exactly 16 digits")
	private String cardNumber;

	@NotBlank(message = "Card expiry is required")
	@Pattern(regexp = "(0[1-9]|1[0-2])/\\d{2}", message = "Card expiry must be in MM/YY format")
	private String cardExpiry;

	@NotBlank(message = "CVC is required")
	@Pattern(regexp = "\\d{3}", message = "CVC must be exactly 3 digits")
	private String cvc;

	@NotBlank(message = "Cardholder name is required")
	private String cardholderName;
}
