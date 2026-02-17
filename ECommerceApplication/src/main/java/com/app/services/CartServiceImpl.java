package com.app.services;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.entites.Cart;
import com.app.entites.CartItem;
import com.app.entites.Product;
import com.app.entites.PromoCode;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.CartDTO;
import com.app.payloads.ProductDTO;
import com.app.repositories.CartItemRepo;
import com.app.repositories.CartRepo;
import com.app.repositories.ProductRepo;
import com.app.repositories.PromoCodeRepo;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private CartRepo cartRepo;

	@Autowired
	private ProductRepo productRepo;

	@Autowired
	private CartItemRepo cartItemRepo;

	@Autowired
	private PromoCodeRepo promoCodeRepo;

	@Autowired
	private PromoCodeService promoCodeService;

	@Autowired
	private ModelMapper modelMapper;

	@Override
	public CartDTO addProductToCart(Long cartId, Long productId, Integer quantity) {

		Cart cart = cartRepo.findById(cartId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

		Product product = productRepo.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

		CartItem cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId);

		if (cartItem != null) {
			throw new APIException("Product " + product.getProductName() + " already exists in the cart");
		}

		if (product.getQuantity() == 0) {
			throw new APIException(product.getProductName() + " is not available");
		}

		if (product.getQuantity() < quantity) {
			throw new APIException("Please, make an order of the " + product.getProductName()
					+ " less than or equal to the quantity " + product.getQuantity() + ".");
		}

		CartItem newCartItem = new CartItem();

		newCartItem.setProduct(product);
		newCartItem.setCart(cart);
		newCartItem.setQuantity(quantity);
		newCartItem.setDiscount(product.getDiscount());
		newCartItem.setProductPrice(product.getSpecialPrice());

		cartItemRepo.save(newCartItem);

		product.setQuantity(product.getQuantity() - quantity);

		// Update totalPrice (with product discount)
		cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
		
		// Update originalTotalPrice (without product discount) - for promo code calculation (requirement f)
		Double currentOriginalTotal = cart.getOriginalTotalPrice() != null ? cart.getOriginalTotalPrice() : 0.0;
		cart.setOriginalTotalPrice(currentOriginalTotal + (product.getPrice() * quantity));
		
		// Recalculate if promo code is applied
		recalculateCartDiscount(cart);

		CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

		List<ProductDTO> productDTOs = cart.getCartItems().stream()
				.map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());

		cartDTO.setProducts(productDTOs);

		return cartDTO;

	}

	@Override
	public List<CartDTO> getAllCarts() {
		List<Cart> carts = cartRepo.findAll();

		if (carts.size() == 0) {
			throw new APIException("No cart exists");
		}

		List<CartDTO> cartDTOs = carts.stream().map(cart -> {
			CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

			List<ProductDTO> products = cart.getCartItems().stream()
					.map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());

			cartDTO.setProducts(products);

			return cartDTO;

		}).collect(Collectors.toList());

		return cartDTOs;
	}

	@Override
	public CartDTO getCart(String email, Long cartId) {
		Cart cart = cartRepo.findCartByEmailAndCartId(email, cartId);

		if (cart == null) {
			throw new ResourceNotFoundException("Cart", "cartId", cartId);
		}

		CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
		
		List<ProductDTO> products = cart.getCartItems().stream()
				.map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());

		cartDTO.setProducts(products);

		return cartDTO;
	}

	@Override
	public void updateProductInCarts(Long cartId, Long productId) {
		Cart cart = cartRepo.findById(cartId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

		Product product = productRepo.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

		CartItem cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId);

		if (cartItem == null) {
			throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
		}

		double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());

		cartItem.setProductPrice(product.getSpecialPrice());

		cart.setTotalPrice(cartPrice + (cartItem.getProductPrice() * cartItem.getQuantity()));

		cartItem = cartItemRepo.save(cartItem);
	}

	@Override
	public CartDTO updateProductQuantityInCart(Long cartId, Long productId, Integer quantity) {
		Cart cart = cartRepo.findById(cartId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

		Product product = productRepo.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

		if (product.getQuantity() == 0) {
			throw new APIException(product.getProductName() + " is not available");
		}

		if (product.getQuantity() < quantity) {
			throw new APIException("Please, make an order of the " + product.getProductName()
					+ " less than or equal to the quantity " + product.getQuantity() + ".");
		}

		CartItem cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId);

		if (cartItem == null) {
			throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
		}

		// Update totalPrice (with product discount)
		double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());

		// Update originalTotalPrice (without product discount) - requirement f
		Double currentOriginalTotal = cart.getOriginalTotalPrice() != null ? cart.getOriginalTotalPrice() : 0.0;
		Double originalPrice = currentOriginalTotal - (product.getPrice() * cartItem.getQuantity());
		cart.setOriginalTotalPrice(originalPrice + (product.getPrice() * quantity));

		product.setQuantity(product.getQuantity() + cartItem.getQuantity() - quantity);

		cartItem.setProductPrice(product.getSpecialPrice());
		cartItem.setQuantity(quantity);
		cartItem.setDiscount(product.getDiscount());

		cart.setTotalPrice(cartPrice + (cartItem.getProductPrice() * quantity));

		cartItem = cartItemRepo.save(cartItem);

		// Recalculate if promo code is applied
		recalculateCartDiscount(cart);

		CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

		List<ProductDTO> productDTOs = cart.getCartItems().stream()
				.map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());

		cartDTO.setProducts(productDTOs);

		return cartDTO;

	}

	@Override
	public String deleteProductFromCart(Long cartId, Long productId) {
		Cart cart = cartRepo.findById(cartId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

		CartItem cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId);

		if (cartItem == null) {
			throw new ResourceNotFoundException("Product", "productId", productId);
		}

		cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));
		
		// Update originalTotalPrice (requirement f)
		Product product = cartItem.getProduct();
		Double currentOriginalTotal = cart.getOriginalTotalPrice() != null ? cart.getOriginalTotalPrice() : 0.0;
		cart.setOriginalTotalPrice(currentOriginalTotal - (product.getPrice() * cartItem.getQuantity()));

		product.setQuantity(product.getQuantity() + cartItem.getQuantity());

		cartItemRepo.deleteCartItemByProductIdAndCartId(cartId, productId);

		// Recalculate discount if promo code is applied
		recalculateCartDiscount(cart);

		return "Product " + cartItem.getProduct().getProductName() + " removed from the cart !!!";
	}

	/**
	 * Apply promo code to cart.
	 * 
	 * Requirement (f): When promo code is applied, product discount is NOT counted.
	 * Uses originalTotalPrice (without product discount) for calculation.
	 */
	@Override
	public CartDTO applyPromoCode(Long cartId, String promoCode) {
		Cart cart = cartRepo.findById(cartId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

		// Use originalTotalPrice for validation and calculation (requirement f)
		Double baseAmount = cart.getOriginalTotalPrice() != null && cart.getOriginalTotalPrice() > 0 
				? cart.getOriginalTotalPrice() 
				: cart.getTotalPrice();

		// Validate the promo code against original price
		promoCodeService.validatePromoCode(promoCode, baseAmount);

		PromoCode promo = promoCodeRepo.findByCode(promoCode)
				.orElseThrow(() -> new APIException("Invalid promo code: " + promoCode));

		// Apply the promo code (requirement f: use original price, not specialPrice)
		cart.setAppliedPromoCode(promo);
		Double discountAmount = promoCodeService.calculateDiscount(promoCode, baseAmount);
		cart.setDiscountAmount(discountAmount);
		// Final price = original price - promo discount (product discount not counted per requirement f)
		cart.setFinalPrice(baseAmount - discountAmount);

		Cart savedCart = cartRepo.save(cart);

		return mapCartToDTO(savedCart);
	}

	@Override
	public CartDTO removePromoCode(Long cartId) {
		Cart cart = cartRepo.findById(cartId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

		cart.setAppliedPromoCode(null);
		cart.setDiscountAmount(0.0);
		// When promo removed, use totalPrice (with product discount) again
		cart.setFinalPrice(cart.getTotalPrice());

		Cart savedCart = cartRepo.save(cart);

		return mapCartToDTO(savedCart);
	}

	/**
	 * Recalculate cart discount.
	 * 
	 * Requirement (f): When promo code is applied, product discount is NOT counted.
	 * Uses originalTotalPrice for promo code calculation.
	 */
	private void recalculateCartDiscount(Cart cart) {
		if (cart.getAppliedPromoCode() != null) {
			try {
				// Use originalTotalPrice for promo code calculation (requirement f)
				Double baseAmount = cart.getOriginalTotalPrice() != null && cart.getOriginalTotalPrice() > 0 
						? cart.getOriginalTotalPrice() 
						: cart.getTotalPrice();
				
				Double discountAmount = promoCodeService.calculateDiscount(
						cart.getAppliedPromoCode().getCode(), baseAmount);
				cart.setDiscountAmount(discountAmount);
				// Final price = original price - promo discount
				cart.setFinalPrice(baseAmount - discountAmount);
			} catch (APIException e) {
				// If promo code is no longer valid, remove it
				cart.setAppliedPromoCode(null);
				cart.setDiscountAmount(0.0);
				cart.setFinalPrice(cart.getTotalPrice());
			}
		} else {
			// No promo code, use totalPrice (with product discount)
			cart.setFinalPrice(cart.getTotalPrice());
		}
		cartRepo.save(cart);
	}

	private CartDTO mapCartToDTO(Cart cart) {
		CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
		
		List<ProductDTO> products = cart.getCartItems().stream()
				.map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());
		
		cartDTO.setProducts(products);
		
		if (cart.getAppliedPromoCode() != null) {
			cartDTO.setAppliedPromoCode(cart.getAppliedPromoCode().getCode());
		}
		
		cartDTO.setOriginalTotalPrice(cart.getOriginalTotalPrice() != null ? cart.getOriginalTotalPrice() : 0.0);
		cartDTO.setDiscountAmount(cart.getDiscountAmount() != null ? cart.getDiscountAmount() : 0.0);
		cartDTO.setFinalPrice(cart.getFinalPrice() != null ? cart.getFinalPrice() : cart.getTotalPrice());
		
		return cartDTO;
	}

}
