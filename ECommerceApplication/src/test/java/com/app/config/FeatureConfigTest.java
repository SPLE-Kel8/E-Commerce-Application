package com.app.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.app.payloads.BankAccountDTO;

/**
 * Test untuk FeatureConfig - validasi fitur payment dan promo code.
 * 
 * Sesuai deskripsi:
 * - Pembayaran hanya dengan transfer bank (d)
 * - Diskon dengan kode promo (h)
 */
@DisplayName("FeatureConfig Tests")
class FeatureConfigTest {

    private FeatureConfig featureConfig;

    @BeforeEach
    void setUp() {
        featureConfig = new FeatureConfig();
        // Set up multiple banks config as in application.properties
        featureConfig.setSupportedBanksConfig("BCA:Bank Central Asia:1234567890:Toko E-Commerce,BNI:Bank Negara Indonesia:0987654321:Toko E-Commerce");
        featureConfig.initBanks();
    }

    @Nested
    @DisplayName("Payment Method Tests - Bank Transfer Only")
    class PaymentMethodTests {

        @BeforeEach
        void setUpPaymentConfig() {
            // Konfigurasi sesuai requirement: hanya Bank Transfer
            featureConfig.setBankTransferEnabled(true);
            featureConfig.setCreditCardEnabled(false);
            featureConfig.setEWalletEnabled(false);
        }

        @Test
        @DisplayName("BANK_TRANSFER harus enabled")
        void bankTransfer_ShouldBeEnabled() {
            assertTrue(featureConfig.isPaymentMethodEnabled("BANK_TRANSFER"));
        }

        @Test
        @DisplayName("BANK_TRANSFER case insensitive harus work")
        void bankTransfer_CaseInsensitive_ShouldWork() {
            assertTrue(featureConfig.isPaymentMethodEnabled("bank_transfer"));
            assertTrue(featureConfig.isPaymentMethodEnabled("Bank_Transfer"));
        }

        @Test
        @DisplayName("CREDIT_CARD harus disabled")
        void creditCard_ShouldBeDisabled() {
            assertFalse(featureConfig.isPaymentMethodEnabled("CREDIT_CARD"));
        }

        @Test
        @DisplayName("E_WALLET harus disabled")
        void eWallet_ShouldBeDisabled() {
            assertFalse(featureConfig.isPaymentMethodEnabled("E_WALLET"));
        }

        @Test
        @DisplayName("Payment method tidak dikenal harus return false")
        void unknownPaymentMethod_ShouldReturnFalse() {
            assertFalse(featureConfig.isPaymentMethodEnabled("PAYPAL"));
            assertFalse(featureConfig.isPaymentMethodEnabled("CRYPTO"));
            assertFalse(featureConfig.isPaymentMethodEnabled("COD"));
        }

        @Test
        @DisplayName("Null payment method harus return false")
        void nullPaymentMethod_ShouldReturnFalse() {
            assertFalse(featureConfig.isPaymentMethodEnabled(null));
        }

        @Test
        @DisplayName("Message harus menunjukkan BANK_TRANSFER saja")
        void supportedPaymentMethodsMessage_ShouldShowBankTransferOnly() {
            String message = featureConfig.getSupportedPaymentMethodsMessage();
            
            assertTrue(message.contains("BANK_TRANSFER"));
            assertFalse(message.contains("CREDIT_CARD"));
            assertFalse(message.contains("E_WALLET"));
        }
    }

    @Nested
    @DisplayName("Promo Code Feature Tests")
    class PromoCodeFeatureTests {

        @Test
        @DisplayName("Promo code feature harus enabled by default")
        void promoCodeFeature_ShouldBeEnabledByDefault() {
            featureConfig.setPromoCodeEnabled(true);
            assertTrue(featureConfig.isPromoCodeEnabled());
        }

        @Test
        @DisplayName("Promo code feature dapat di-disable")
        void promoCodeFeature_CanBeDisabled() {
            featureConfig.setPromoCodeEnabled(false);
            assertFalse(featureConfig.isPromoCodeEnabled());
        }
    }

    @Nested
    @DisplayName("Bank Selection Tests - Requirement (d)")
    class BankSelectionTests {

        @Test
        @DisplayName("Berhasil mendapatkan daftar bank yang didukung")
        void getSupportedBankList_ReturnsConfiguredBanks() {
            List<BankAccountDTO> banks = featureConfig.getSupportedBankList();
            
            assertNotNull(banks);
            assertEquals(2, banks.size());
        }

        @Test
        @DisplayName("Berhasil mendapatkan detail bank berdasarkan kode")
        void getBankAccount_ReturnsCorrectDetails() {
            BankAccountDTO bca = featureConfig.getBankAccount("BCA");
            
            assertNotNull(bca);
            assertEquals("BCA", bca.getBankCode());
            assertEquals("Bank Central Asia", bca.getBankName());
            assertEquals("1234567890", bca.getAccountNumber());
            assertEquals("Toko E-Commerce", bca.getAccountName());
        }

        @Test
        @DisplayName("Bank code case insensitive")
        void getBankAccount_CaseInsensitive() {
            BankAccountDTO bca = featureConfig.getBankAccount("bca");
            assertNotNull(bca);
            assertEquals("BCA", bca.getBankCode());
        }

        @Test
        @DisplayName("Bank tidak ditemukan return null")
        void getBankAccount_NotFound_ReturnsNull() {
            BankAccountDTO unknown = featureConfig.getBankAccount("UNKNOWN");
            assertNull(unknown);
        }

        @Test
        @DisplayName("isBankSupported check bank code")
        void isBankSupported_ChecksBankCode() {
            assertTrue(featureConfig.isBankSupported("BCA"));
            assertTrue(featureConfig.isBankSupported("BNI"));
            assertFalse(featureConfig.isBankSupported("MANDIRI")); // not configured in test
            assertFalse(featureConfig.isBankSupported(null));
        }

        @Test
        @DisplayName("getSupportedBanksMessage menampilkan daftar bank")
        void getSupportedBanksMessage_ShowsBankList() {
            String message = featureConfig.getSupportedBanksMessage();
            
            assertTrue(message.contains("BCA"));
            assertTrue(message.contains("BNI"));
        }
    }

    @Nested
    @DisplayName("Multiple Payment Methods Scenario (untuk Clone-and-Own)")
    class MultiplePaymentMethodsTests {

        @Test
        @DisplayName("Jika semua payment enabled, message harus lengkap")
        void allPaymentsEnabled_MessageShouldBeComplete() {
            featureConfig.setBankTransferEnabled(true);
            featureConfig.setCreditCardEnabled(true);
            featureConfig.setEWalletEnabled(true);

            String message = featureConfig.getSupportedPaymentMethodsMessage();
            
            assertTrue(message.contains("BANK_TRANSFER"));
            assertTrue(message.contains("CREDIT_CARD"));
            assertTrue(message.contains("E_WALLET"));
        }

        @Test
        @DisplayName("Jika hanya E_WALLET enabled")
        void onlyEWalletEnabled_ShouldWork() {
            featureConfig.setBankTransferEnabled(false);
            featureConfig.setCreditCardEnabled(false);
            featureConfig.setEWalletEnabled(true);

            assertTrue(featureConfig.isPaymentMethodEnabled("E_WALLET"));
            assertFalse(featureConfig.isPaymentMethodEnabled("BANK_TRANSFER"));
            assertFalse(featureConfig.isPaymentMethodEnabled("CREDIT_CARD"));
        }
    }
}
