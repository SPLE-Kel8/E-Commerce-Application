package com.app.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payment DTO - includes bank transfer details per requirement (d).
 * 
 * "Sistem lalu memberikan nomor rekening toko untuk nama bank yang dipilih."
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
	private Long paymentId;
	private String paymentMethod;
	
	/** Bank code for bank transfer (e.g., BCA, BNI) */
	private String bankCode;
	
	/** Bank name for display */
	private String bankName;
	
	/** Account number to transfer to - returned by system */
	private String bankAccountNumber;
	
	/** Account name for verification */
	private String bankAccountName;
}
