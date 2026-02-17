package com.app.entites;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cart entity - shopping cart for user.
 * 
 * Requirement (f): When promo code is applied, product discount is NOT counted.
 * Uses originalTotalPrice for promo code calculation.
 */
@Entity
@Data
@Table(name = "carts")
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long cartId;

	@OneToOne
	@JoinColumn(name = "user_id")
	private User user;

	@OneToMany(mappedBy = "cart", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
	private List<CartItem> cartItems = new ArrayList<>();

	/** Total price using product's specialPrice (with product discount) */
	private Double totalPrice = 0.0;
	
	/** Total price using original product price (without product discount) - for promo code calculation (f) */
	private Double originalTotalPrice = 0.0;

	@ManyToOne
	@JoinColumn(name = "promo_code_id")
	private PromoCode appliedPromoCode;

	private Double discountAmount = 0.0;

	private Double finalPrice = 0.0;
}