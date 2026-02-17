package com.app.entites;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payment entity - stores payment information for orders.
 * 
 * Requirement (d): For bank transfer, stores the selected bank code.
 */
@Entity
@Data
@Table(name = "payments")
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long paymentId;

	@OneToOne(mappedBy = "payment", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Order order;

	@NotBlank
	@Size(min = 4, message = "Payment method must contain atleast 4 characters")
	private String paymentMethod;
	
	/** Bank code for bank transfer payments (e.g., BCA, BNI, MANDIRI) - Requirement (d) */
	private String bankCode;
	
	/** Bank name for reference */
	private String bankName;
	
	/** Account number for the selected bank */
	private String bankAccountNumber;
	
	/** Account name for the selected bank */
	private String bankAccountName;
}
