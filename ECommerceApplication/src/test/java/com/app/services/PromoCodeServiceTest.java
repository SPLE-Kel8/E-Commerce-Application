package com.app.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.app.entites.PromoCode;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.PromoCodeDTO;
import com.app.repositories.PromoCodeRepo;

/**
 * Test untuk PromoCodeService - fitur diskon dengan kode promo.
 * 
 * Sesuai deskripsi:
 * - Diskon dengan kode promo
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PromoCodeService Tests")
class PromoCodeServiceTest {

    @Mock
    private PromoCodeRepo promoCodeRepo;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PromoCodeServiceImpl promoCodeService;

    private PromoCode validPromoCode;
    private PromoCodeDTO promoCodeDTO;

    @BeforeEach
    void setUp() {
        validPromoCode = new PromoCode();
        validPromoCode.setPromoCodeId(1L);
        validPromoCode.setCode("DISKON10");
        validPromoCode.setDescription("Diskon 10% untuk semua produk");
        validPromoCode.setDiscountPercentage(10.0);
        validPromoCode.setStartDate(LocalDate.now().minusDays(1));
        validPromoCode.setEndDate(LocalDate.now().plusDays(30));
        validPromoCode.setIsActive(true);
        validPromoCode.setMinimumOrderAmount(50000.0);
        validPromoCode.setUsageLimit(100);
        validPromoCode.setUsedCount(0);

        promoCodeDTO = new PromoCodeDTO();
        promoCodeDTO.setPromoCodeId(1L);
        promoCodeDTO.setCode("DISKON10");
        promoCodeDTO.setDescription("Diskon 10% untuk semua produk");
        promoCodeDTO.setDiscountPercentage(10.0);
        promoCodeDTO.setStartDate(LocalDate.now().minusDays(1));
        promoCodeDTO.setEndDate(LocalDate.now().plusDays(30));
        promoCodeDTO.setIsActive(true);
        promoCodeDTO.setMinimumOrderAmount(50000.0);
        promoCodeDTO.setUsageLimit(100);
        promoCodeDTO.setUsedCount(0);
    }

    @Nested
    @DisplayName("Create Promo Code Tests")
    class CreatePromoCodeTests {

        @Test
        @DisplayName("Berhasil membuat promo code baru")
        void createPromoCode_Success() {
            when(promoCodeRepo.existsByCode("DISKON10")).thenReturn(false);
            when(modelMapper.map(any(PromoCodeDTO.class), eq(PromoCode.class))).thenReturn(validPromoCode);
            when(promoCodeRepo.save(any(PromoCode.class))).thenReturn(validPromoCode);
            when(modelMapper.map(any(PromoCode.class), eq(PromoCodeDTO.class))).thenReturn(promoCodeDTO);

            PromoCodeDTO result = promoCodeService.createPromoCode(promoCodeDTO);

            assertNotNull(result);
            assertEquals("DISKON10", result.getCode());
            verify(promoCodeRepo).save(any(PromoCode.class));
        }

        @Test
        @DisplayName("Gagal membuat promo code yang sudah ada")
        void createPromoCode_DuplicateCode_ThrowsException() {
            when(promoCodeRepo.existsByCode("DISKON10")).thenReturn(true);

            APIException exception = assertThrows(APIException.class, 
                () -> promoCodeService.createPromoCode(promoCodeDTO));

            assertTrue(exception.getMessage().contains("already exists"));
        }
    }

    @Nested
    @DisplayName("Validate Promo Code Tests")
    class ValidatePromoCodeTests {

        @Test
        @DisplayName("Validasi promo code aktif berhasil")
        void validatePromoCode_ActiveCode_Success() {
            when(promoCodeRepo.findByCode("DISKON10")).thenReturn(Optional.of(validPromoCode));
            when(modelMapper.map(any(PromoCode.class), eq(PromoCodeDTO.class))).thenReturn(promoCodeDTO);

            PromoCodeDTO result = promoCodeService.validatePromoCode("DISKON10", 100000.0);

            assertNotNull(result);
            assertEquals("DISKON10", result.getCode());
        }

        @Test
        @DisplayName("Validasi promo code tidak aktif gagal")
        void validatePromoCode_InactiveCode_ThrowsException() {
            validPromoCode.setIsActive(false);
            when(promoCodeRepo.findByCode("DISKON10")).thenReturn(Optional.of(validPromoCode));

            APIException exception = assertThrows(APIException.class,
                () -> promoCodeService.validatePromoCode("DISKON10", 100000.0));

            assertEquals("Promo code is not active", exception.getMessage());
        }

        @Test
        @DisplayName("Validasi promo code belum berlaku gagal")
        void validatePromoCode_NotYetValid_ThrowsException() {
            validPromoCode.setStartDate(LocalDate.now().plusDays(1));
            when(promoCodeRepo.findByCode("DISKON10")).thenReturn(Optional.of(validPromoCode));

            APIException exception = assertThrows(APIException.class,
                () -> promoCodeService.validatePromoCode("DISKON10", 100000.0));

            assertEquals("Promo code is not yet valid", exception.getMessage());
        }

        @Test
        @DisplayName("Validasi promo code expired gagal")
        void validatePromoCode_Expired_ThrowsException() {
            validPromoCode.setEndDate(LocalDate.now().minusDays(1));
            when(promoCodeRepo.findByCode("DISKON10")).thenReturn(Optional.of(validPromoCode));

            APIException exception = assertThrows(APIException.class,
                () -> promoCodeService.validatePromoCode("DISKON10", 100000.0));

            assertEquals("Promo code has expired", exception.getMessage());
        }

        @Test
        @DisplayName("Validasi promo code limit tercapai gagal")
        void validatePromoCode_UsageLimitReached_ThrowsException() {
            validPromoCode.setUsageLimit(10);
            validPromoCode.setUsedCount(10);
            when(promoCodeRepo.findByCode("DISKON10")).thenReturn(Optional.of(validPromoCode));

            APIException exception = assertThrows(APIException.class,
                () -> promoCodeService.validatePromoCode("DISKON10", 100000.0));

            assertEquals("Promo code usage limit reached", exception.getMessage());
        }

        @Test
        @DisplayName("Validasi promo code minimum order tidak tercapai gagal")
        void validatePromoCode_MinimumOrderNotMet_ThrowsException() {
            validPromoCode.setMinimumOrderAmount(100000.0);
            when(promoCodeRepo.findByCode("DISKON10")).thenReturn(Optional.of(validPromoCode));

            APIException exception = assertThrows(APIException.class,
                () -> promoCodeService.validatePromoCode("DISKON10", 50000.0));

            assertTrue(exception.getMessage().contains("Minimum order amount"));
        }

        @Test
        @DisplayName("Validasi promo code tidak ditemukan gagal")
        void validatePromoCode_NotFound_ThrowsException() {
            when(promoCodeRepo.findByCode("INVALID")).thenReturn(Optional.empty());

            APIException exception = assertThrows(APIException.class,
                () -> promoCodeService.validatePromoCode("INVALID", 100000.0));

            assertTrue(exception.getMessage().contains("Invalid promo code"));
        }
    }

    @Nested
    @DisplayName("Calculate Discount Tests")
    class CalculateDiscountTests {

        @Test
        @DisplayName("Hitung diskon 10% berhasil")
        void calculateDiscount_10Percent_Success() {
            validPromoCode.setDiscountPercentage(10.0);
            when(promoCodeRepo.findByCode("DISKON10")).thenReturn(Optional.of(validPromoCode));
            when(modelMapper.map(any(PromoCode.class), eq(PromoCodeDTO.class))).thenReturn(promoCodeDTO);

            Double discount = promoCodeService.calculateDiscount("DISKON10", 100000.0);

            assertEquals(10000.0, discount);
        }

        @Test
        @DisplayName("Hitung diskon 25% berhasil")
        void calculateDiscount_25Percent_Success() {
            validPromoCode.setDiscountPercentage(25.0);
            when(promoCodeRepo.findByCode("DISKON25")).thenReturn(Optional.of(validPromoCode));
            promoCodeDTO.setDiscountPercentage(25.0);
            when(modelMapper.map(any(PromoCode.class), eq(PromoCodeDTO.class))).thenReturn(promoCodeDTO);

            Double discount = promoCodeService.calculateDiscount("DISKON25", 200000.0);

            assertEquals(50000.0, discount);
        }

        @Test
        @DisplayName("Hitung diskon 50% berhasil")
        void calculateDiscount_50Percent_Success() {
            validPromoCode.setDiscountPercentage(50.0);
            when(promoCodeRepo.findByCode("DISKON50")).thenReturn(Optional.of(validPromoCode));
            promoCodeDTO.setDiscountPercentage(50.0);
            when(modelMapper.map(any(PromoCode.class), eq(PromoCodeDTO.class))).thenReturn(promoCodeDTO);

            Double discount = promoCodeService.calculateDiscount("DISKON50", 100000.0);

            assertEquals(50000.0, discount);
        }
    }

    @Nested
    @DisplayName("Get All Promo Codes Tests")
    class GetAllPromoCodesTests {

        @Test
        @DisplayName("Berhasil mendapatkan semua promo codes")
        void getAllPromoCodes_Success() {
            List<PromoCode> promoCodes = Arrays.asList(validPromoCode);
            when(promoCodeRepo.findAll()).thenReturn(promoCodes);
            when(modelMapper.map(any(PromoCode.class), eq(PromoCodeDTO.class))).thenReturn(promoCodeDTO);

            List<PromoCodeDTO> result = promoCodeService.getAllPromoCodes();

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Tidak ada promo codes throws exception")
        void getAllPromoCodes_Empty_ThrowsException() {
            when(promoCodeRepo.findAll()).thenReturn(Arrays.asList());

            APIException exception = assertThrows(APIException.class,
                () -> promoCodeService.getAllPromoCodes());

            assertEquals("No promo codes found", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Delete Promo Code Tests")
    class DeletePromoCodeTests {

        @Test
        @DisplayName("Berhasil hapus promo code")
        void deletePromoCode_Success() {
            when(promoCodeRepo.findById(1L)).thenReturn(Optional.of(validPromoCode));

            String result = promoCodeService.deletePromoCode(1L);

            assertTrue(result.contains("deleted successfully"));
            verify(promoCodeRepo).delete(validPromoCode);
        }

        @Test
        @DisplayName("Hapus promo code tidak ditemukan throws exception")
        void deletePromoCode_NotFound_ThrowsException() {
            when(promoCodeRepo.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                () -> promoCodeService.deletePromoCode(999L));
        }
    }

    @Nested
    @DisplayName("Update Promo Code Tests")
    class UpdatePromoCodeTests {

        @Test
        @DisplayName("Berhasil update promo code")
        void updatePromoCode_Success() {
            PromoCodeDTO updateDTO = new PromoCodeDTO();
            updateDTO.setCode("DISKON10");
            updateDTO.setDescription("Updated description");
            updateDTO.setDiscountPercentage(15.0);
            updateDTO.setIsActive(true);

            when(promoCodeRepo.findById(1L)).thenReturn(Optional.of(validPromoCode));
            when(promoCodeRepo.save(any(PromoCode.class))).thenReturn(validPromoCode);
            when(modelMapper.map(any(PromoCode.class), eq(PromoCodeDTO.class))).thenReturn(updateDTO);

            PromoCodeDTO result = promoCodeService.updatePromoCode(1L, updateDTO);

            assertNotNull(result);
            verify(promoCodeRepo).save(any(PromoCode.class));
        }
    }
}
