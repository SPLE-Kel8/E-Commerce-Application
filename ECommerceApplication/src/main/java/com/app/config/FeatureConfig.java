package com.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Feature configuration for Clone-and-Own SPLE.
 * 
 * This class centralizes all feature flags and configuration values.
 * When cloning this project for a new product variant:
 * 1. Modify application.properties to enable/disable features
 * 2. No code changes needed - features auto-configure based on properties
 * 
 * Example: To enable credit card payments:
 * - Set app.payment.credit-card.enabled=true in application.properties
 * - Add "CREDIT_CARD" to AppConstants.SUPPORTED_PAYMENT_METHODS
 */
@Configuration
@Data
public class FeatureConfig {

    // ==================== PAYMENT FEATURES ====================
    @Value("${app.payment.bank-transfer.enabled:true}")
    private boolean bankTransferEnabled;

    @Value("${app.payment.credit-card.enabled:false}")
    private boolean creditCardEnabled;

    @Value("${app.payment.e-wallet.enabled:false}")
    private boolean eWalletEnabled;

    // ==================== CORE FEATURES ====================
    @Value("${app.feature.promo-code.enabled:true}")
    private boolean promoCodeEnabled;

    @Value("${app.feature.product-discount.enabled:true}")
    private boolean productDiscountEnabled;

    // ==================== BANK TRANSFER CONFIG ====================
    @Value("${app.payment.bank-transfer.bank-name:Bank Central}")
    private String bankName;

    @Value("${app.payment.bank-transfer.account-number:1234567890}")
    private String accountNumber;

    @Value("${app.payment.bank-transfer.account-name:E-Commerce Store}")
    private String accountName;

    /**
     * Check if a payment method is enabled
     */
    public boolean isPaymentMethodEnabled(String paymentMethod) {
        if (paymentMethod == null) {
            return false;
        }
        
        return switch (paymentMethod.toUpperCase()) {
            case "BANK_TRANSFER" -> bankTransferEnabled;
            case "CREDIT_CARD" -> creditCardEnabled;
            case "E_WALLET" -> eWalletEnabled;
            default -> false;
        };
    }

    /**
     * Get descriptive error message for unsupported payment methods
     */
    public String getSupportedPaymentMethodsMessage() {
        StringBuilder sb = new StringBuilder("Supported payment methods: ");
        boolean first = true;
        
        if (bankTransferEnabled) {
            sb.append("BANK_TRANSFER");
            first = false;
        }
        if (creditCardEnabled) {
            if (!first) sb.append(", ");
            sb.append("CREDIT_CARD");
            first = false;
        }
        if (eWalletEnabled) {
            if (!first) sb.append(", ");
            sb.append("E_WALLET");
        }
        
        return sb.toString();
    }
}
