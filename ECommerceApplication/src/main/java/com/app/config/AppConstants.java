package com.app.config;

/**
 * Application constants for easy configuration in Clone-and-Own SPLE.
 * 
 * CUSTOMIZATION GUIDE:
 * - Pagination: Modify PAGE_NUMBER, PAGE_SIZE for default pagination behavior
 * - Sorting: Modify SORT_*_BY constants for default sorting fields
 * - Security: Modify JWT_TOKEN_VALIDITY, PUBLIC_URLS, USER_URLS, ADMIN_URLS
 * - Payment: Modify SUPPORTED_PAYMENT_METHODS to add/remove payment options
 * - PromoCode: Modify promo code constraints as needed
 */
public class AppConstants {
	
	// ==================== PAGINATION ====================
	public static final String PAGE_NUMBER = "0";
	public static final String PAGE_SIZE = "2";
	
	// ==================== SORTING ====================
	public static final String SORT_CATEGORIES_BY = "categoryId";
	public static final String SORT_PRODUCTS_BY = "productId";
	public static final String SORT_USERS_BY = "userId";
	public static final String SORT_ORDERS_BY = "totalAmount";
	public static final String SORT_DIR = "asc";
	
	// ==================== USER ROLES ====================
	public static final Long ADMIN_ID = 101L;
	public static final Long USER_ID = 102L;
	
	// ==================== JWT SECURITY ====================
	public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;
	
	// ==================== URL SECURITY ====================
	public static final String[] PUBLIC_URLS = { "/v3/api-docs/**", "/swagger-ui/**", "/api/register/**", "/api/login" };
	public static final String[] USER_URLS = { "/api/public/**" };
	public static final String[] ADMIN_URLS = { "/api/admin/**" };
	
	// ==================== PAYMENT CONFIGURATION ====================
	// For Clone-and-Own: Add/remove payment methods here
	// Current configuration: Bank Transfer only (as per requirements)
	public static final String PAYMENT_BANK_TRANSFER = "BANK_TRANSFER";
	public static final String[] SUPPORTED_PAYMENT_METHODS = { PAYMENT_BANK_TRANSFER };
	public static final String DEFAULT_PAYMENT_METHOD = PAYMENT_BANK_TRANSFER;
	
	// ==================== PROMO CODE CONFIGURATION ====================
	public static final int PROMO_CODE_MIN_LENGTH = 3;
	public static final int PROMO_CODE_MAX_LENGTH = 20;
	public static final double PROMO_MIN_DISCOUNT_PERCENTAGE = 1.0;
	public static final double PROMO_MAX_DISCOUNT_PERCENTAGE = 100.0;
	
	// ==================== ORDER STATUS ====================
	public static final String ORDER_STATUS_ACCEPTED = "Order Accepted !";
	public static final String ORDER_STATUS_PROCESSING = "Processing";
	public static final String ORDER_STATUS_SHIPPED = "Shipped";
	public static final String ORDER_STATUS_DELIVERED = "Delivered";
	public static final String ORDER_STATUS_CANCELLED = "Cancelled";
	
}
