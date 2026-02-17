package com.app.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.app.payloads.BankAccountDTO;

import jakarta.annotation.PostConstruct;
import lombok.Data;

/**
 * Feature configuration for Clone-and-Own SPLE.
 * 
 * This class centralizes all feature flags and configuration values.
 * When cloning this project for a new product variant:
 * 1. Modify application.properties to enable/disable features
 * 2. No code changes needed - features auto-configure based on properties
 * 
 * Requirement (d): Bank transfer - customer selects bank, system provides account number
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

    // ==================== BANK TRANSFER CONFIG (Requirement d) ====================
    @Value("${app.payment.bank-transfer.supported-banks:}")
    private String supportedBanksConfig;

    /** Map of bank code to BankAccountDTO */
    private Map<String, BankAccountDTO> supportedBanks = new HashMap<>();

    @PostConstruct
    public void initBanks() {
        if (supportedBanksConfig != null && !supportedBanksConfig.trim().isEmpty()) {
            String[] banks = supportedBanksConfig.split(",");
            for (String bank : banks) {
                String[] parts = bank.trim().split(":");
                if (parts.length >= 4) {
                    BankAccountDTO dto = new BankAccountDTO(
                        parts[0].trim(),  // bankCode
                        parts[1].trim(),  // bankName
                        parts[2].trim(),  // accountNumber
                        parts[3].trim()   // accountName
                    );
                    supportedBanks.put(parts[0].trim().toUpperCase(), dto);
                }
            }
        }
    }

    /**
     * Check if a payment method is enabled
     */
    public boolean isPaymentMethodEnabled(String paymentMethod) {
        if (paymentMethod == null) {
            return false;
        }
        
        switch (paymentMethod.toUpperCase()) {
            case "BANK_TRANSFER":
                return bankTransferEnabled;
            case "CREDIT_CARD":
                return creditCardEnabled;
            case "E_WALLET":
                return eWalletEnabled;
            default:
                return false;
        }
    }

    /**
     * Get list of supported banks (Requirement d)
     */
    public List<BankAccountDTO> getSupportedBankList() {
        return new ArrayList<>(supportedBanks.values());
    }

    /**
     * Get bank account details by bank code (Requirement d)
     * @param bankCode e.g., "BCA", "BNI", "MANDIRI"
     * @return BankAccountDTO with account details, or null if not found
     */
    public BankAccountDTO getBankAccount(String bankCode) {
        if (bankCode == null) return null;
        return supportedBanks.get(bankCode.toUpperCase());
    }

    /**
     * Check if bank code is supported (Requirement d)
     */
    public boolean isBankSupported(String bankCode) {
        return bankCode != null && supportedBanks.containsKey(bankCode.toUpperCase());
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

    /**
     * Get list of supported bank codes
     */
    public String getSupportedBanksMessage() {
        if (supportedBanks.isEmpty()) {
            return "No banks configured";
        }
        return "Supported banks: " + String.join(", ", supportedBanks.keySet());
    }
}
