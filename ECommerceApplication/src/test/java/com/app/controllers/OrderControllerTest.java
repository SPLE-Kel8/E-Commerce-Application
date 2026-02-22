package com.app.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.app.config.FeatureConfig;
import com.app.exceptions.APIException;
import com.app.payloads.BankAccountDTO;
import com.app.payloads.OrderDTO;
import com.app.payloads.OrderResponse;
import com.app.payloads.PaymentDTO;
import com.app.security.AuthUtil;
import com.app.services.OrderService;

/**
 * Test untuk OrderController - fitur checkout dan pembayaran.
 * 
 * Sesuai deskripsi:
 * - Checkout
 * - Pembayaran standar
 * - Pembayaran hanya dengan transfer bank
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController Tests - Checkout & Payment")
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private FeatureConfig featureConfig;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private OrderController orderController;

    private OrderDTO orderDTO;
    private String testEmail;
    private Long testCartId;

    @BeforeEach
    void setUp() {
        testEmail = "user@test.com";
        testCartId = 1L;

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPaymentId(1L);
        paymentDTO.setPaymentMethod("BANK_TRANSFER");

        orderDTO = new OrderDTO();
        orderDTO.setOrderId(1L);
        orderDTO.setEmail(testEmail);
        orderDTO.setOrderDate(LocalDate.now());
        orderDTO.setPayment(paymentDTO);
        orderDTO.setTotalAmount(100000.0);
        orderDTO.setOrderStatus("Order Accepted !");
        orderDTO.setOrderItems(new ArrayList<>());
    }

    @Nested
    @DisplayName("Payment Method Validation Tests - Bank Transfer Only")
    class PaymentMethodValidationTests {

        @Test
        @DisplayName("Checkout dengan BANK_TRANSFER via legacy endpoint, redirect to new endpoint")
        void orderProducts_BankTransfer_RedirectsToNewEndpoint() {
            when(featureConfig.isPaymentMethodEnabled("BANK_TRANSFER")).thenReturn(true);
            when(featureConfig.getSupportedBanksMessage()).thenReturn("Supported banks: BCA, BNI");

            APIException exception = assertThrows(APIException.class,
                () -> orderController.orderProducts(testEmail, testCartId, "BANK_TRANSFER"));

            assertTrue(exception.getMessage().contains("bank-transfer"));
            assertTrue(exception.getMessage().contains("bankCode"));
        }

        @Test
        @DisplayName("Checkout dengan CREDIT_CARD ditolak")
        void orderProducts_CreditCard_Rejected() {
            when(featureConfig.isPaymentMethodEnabled("CREDIT_CARD")).thenReturn(false);
            when(featureConfig.getSupportedPaymentMethodsMessage()).thenReturn("Supported payment methods: BANK_TRANSFER");

            APIException exception = assertThrows(APIException.class,
                () -> orderController.orderProducts(testEmail, testCartId, "CREDIT_CARD"));

            assertTrue(exception.getMessage().contains("BANK_TRANSFER"));
            verify(orderService, never()).placeOrder(anyString(), any(), anyString());
        }

        @Test
        @DisplayName("Checkout dengan E_WALLET ditolak")
        void orderProducts_EWallet_Rejected() {
            when(featureConfig.isPaymentMethodEnabled("E_WALLET")).thenReturn(false);
            when(featureConfig.getSupportedPaymentMethodsMessage()).thenReturn("Supported payment methods: BANK_TRANSFER");

            APIException exception = assertThrows(APIException.class,
                () -> orderController.orderProducts(testEmail, testCartId, "E_WALLET"));

            assertTrue(exception.getMessage().contains("BANK_TRANSFER"));
            verify(orderService, never()).placeOrder(anyString(), any(), anyString());
        }

        @Test
        @DisplayName("Checkout dengan PAYPAL ditolak")
        void orderProducts_Paypal_Rejected() {
            when(featureConfig.isPaymentMethodEnabled("PAYPAL")).thenReturn(false);
            when(featureConfig.getSupportedPaymentMethodsMessage()).thenReturn("Supported payment methods: BANK_TRANSFER");

            APIException exception = assertThrows(APIException.class,
                () -> orderController.orderProducts(testEmail, testCartId, "PAYPAL"));

            assertTrue(exception.getMessage().contains("BANK_TRANSFER"));
        }

        @Test
        @DisplayName("Checkout dengan COD (Cash on Delivery) ditolak")
        void orderProducts_COD_Rejected() {
            when(featureConfig.isPaymentMethodEnabled("COD")).thenReturn(false);
            when(featureConfig.getSupportedPaymentMethodsMessage()).thenReturn("Supported payment methods: BANK_TRANSFER");

            APIException exception = assertThrows(APIException.class,
                () -> orderController.orderProducts(testEmail, testCartId, "COD"));

            assertTrue(exception.getMessage().contains("BANK_TRANSFER"));
        }

        @Test
        @DisplayName("Checkout dengan payment method kosong ditolak")
        void orderProducts_EmptyPaymentMethod_Rejected() {
            when(featureConfig.isPaymentMethodEnabled("")).thenReturn(false);
            when(featureConfig.getSupportedPaymentMethodsMessage()).thenReturn("Supported payment methods: BANK_TRANSFER");

            APIException exception = assertThrows(APIException.class,
                () -> orderController.orderProducts(testEmail, testCartId, ""));

            assertTrue(exception.getMessage().contains("BANK_TRANSFER"));
        }
    }

    @Nested
    @DisplayName("Order Placement Tests")
    class OrderPlacementTests {

        private BankAccountDTO selectedBank;

        @BeforeEach
        void setUpBankTransfer() {
            selectedBank = new BankAccountDTO("BCA", "Bank Central Asia", "1234567890", "Toko E-Commerce");
            orderDTO.getPayment().setBankCode("BCA");
            orderDTO.getPayment().setBankName("Bank Central Asia");
            orderDTO.getPayment().setBankAccountNumber("1234567890");
        }

        @Test
        @DisplayName("Order berhasil dengan status CREATED via bank transfer endpoint")
        void orderWithBankTransfer_Success_ReturnsCreatedStatus() {
            when(featureConfig.isBankTransferEnabled()).thenReturn(true);
            when(featureConfig.isBankSupported("BCA")).thenReturn(true);
            when(featureConfig.getBankAccount("BCA")).thenReturn(selectedBank);
            when(orderService.placeOrderWithBankTransfer(testEmail, testCartId, selectedBank)).thenReturn(orderDTO);

            ResponseEntity<OrderDTO> response = orderController.orderWithBankTransfer(testEmail, testCartId, "BCA");

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }

        @Test
        @DisplayName("Order memiliki informasi yang lengkap")
        void orderWithBankTransfer_Success_HasCompleteInfo() {
            when(featureConfig.isBankTransferEnabled()).thenReturn(true);
            when(featureConfig.isBankSupported("BCA")).thenReturn(true);
            when(featureConfig.getBankAccount("BCA")).thenReturn(selectedBank);
            when(orderService.placeOrderWithBankTransfer(testEmail, testCartId, selectedBank)).thenReturn(orderDTO);

            ResponseEntity<OrderDTO> response = orderController.orderWithBankTransfer(testEmail, testCartId, "BCA");
            OrderDTO result = response.getBody();

            assertNotNull(result);
            assertNotNull(result.getOrderId());
            assertNotNull(result.getEmail());
            assertNotNull(result.getOrderDate());
            assertNotNull(result.getPayment());
            assertNotNull(result.getTotalAmount());
            assertNotNull(result.getOrderStatus());
            // Bank transfer details
            assertNotNull(result.getPayment().getBankCode());
        }
    }

    @Nested
    @DisplayName("Get Orders Tests")
    class GetOrdersTests {

        @Test
        @DisplayName("Get orders by user berhasil")
        void getOrdersByUser_Success() {
            List<OrderDTO> orders = Arrays.asList(orderDTO);
            when(orderService.getOrdersByUser(testEmail)).thenReturn(orders);

            ResponseEntity<List<OrderDTO>> response = orderController.getOrdersByUser(testEmail);

            assertEquals(HttpStatus.FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
        }

        @Test
        @DisplayName("Get single order by user berhasil")
        void getOrderByUser_Success() {
            when(orderService.getOrder(testEmail, 1L)).thenReturn(orderDTO);

            ResponseEntity<OrderDTO> response = orderController.getOrderByUser(testEmail, 1L);

            assertEquals(HttpStatus.FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1L, response.getBody().getOrderId());
        }

        @Test
        @DisplayName("Get all orders (admin) berhasil")
        void getAllOrders_Success() {
            OrderResponse orderResponse = new OrderResponse();
            orderResponse.setContent(Arrays.asList(orderDTO));
            orderResponse.setPageNumber(0);
            orderResponse.setPageSize(2);
            orderResponse.setTotalElements(1L);
            orderResponse.setTotalPages(1);
            orderResponse.setLastPage(true);

            when(orderService.getAllOrders(0, 2, "totalAmount", "asc")).thenReturn(orderResponse);

            ResponseEntity<OrderResponse> response = orderController.getAllOrders(0, 2, "totalAmount", "asc");

            assertEquals(HttpStatus.FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    @Nested
    @DisplayName("Update Order Status Tests")
    class UpdateOrderStatusTests {

        @Test
        @DisplayName("Update order status berhasil")
        void updateOrderStatus_Success() {
            orderDTO.setOrderStatus("Shipped");
            when(orderService.updateOrder(testEmail, 1L, "Shipped")).thenReturn(orderDTO);

            ResponseEntity<OrderDTO> response = orderController.updateOrderByUser(testEmail, 1L, "Shipped");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Shipped", response.getBody().getOrderStatus());
        }

        @Test
        @DisplayName("Update order status ke Delivered berhasil")
        void updateOrderStatus_Delivered_Success() {
            orderDTO.setOrderStatus("Delivered");
            when(orderService.updateOrder(testEmail, 1L, "Delivered")).thenReturn(orderDTO);

            ResponseEntity<OrderDTO> response = orderController.updateOrderByUser(testEmail, 1L, "Delivered");

            assertEquals("Delivered", response.getBody().getOrderStatus());
        }
    }

    @Nested
    @DisplayName("Order with Promo Code Discount Tests")
    class OrderWithPromoCodeTests {

        @Test
        @DisplayName("Order dengan promo code memiliki discount amount")
        void orderWithBankTransfer_WithPromoCode_HasDiscountAmount() {
            BankAccountDTO selectedBank = new BankAccountDTO("BCA", "Bank Central Asia", "1234567890", "Toko E-Commerce");
            
            orderDTO.setAppliedPromoCode("DISKON10");
            orderDTO.setDiscountAmount(10000.0);
            orderDTO.setTotalAmount(90000.0); // After discount
            orderDTO.getPayment().setBankCode("BCA");

            when(featureConfig.isBankTransferEnabled()).thenReturn(true);
            when(featureConfig.isBankSupported("BCA")).thenReturn(true);
            when(featureConfig.getBankAccount("BCA")).thenReturn(selectedBank);
            when(orderService.placeOrderWithBankTransfer(testEmail, testCartId, selectedBank)).thenReturn(orderDTO);

            ResponseEntity<OrderDTO> response = orderController.orderWithBankTransfer(testEmail, testCartId, "BCA");
            OrderDTO result = response.getBody();

            assertNotNull(result);
            assertEquals("DISKON10", result.getAppliedPromoCode());
            assertEquals(10000.0, result.getDiscountAmount());
            assertEquals(90000.0, result.getTotalAmount());
        }
    }

    @Nested
    @DisplayName("Bank Selection Tests - Requirement (d)")
    class BankSelectionTests {

        @Test
        @DisplayName("Get supported banks berhasil")
        void getSupportedBanks_Success() {
            List<BankAccountDTO> banks = Arrays.asList(
                createBankDTO("BCA", "Bank Central Asia"),
                createBankDTO("BNI", "Bank Negara Indonesia")
            );
            when(featureConfig.isBankTransferEnabled()).thenReturn(true);
            when(featureConfig.getSupportedBankList()).thenReturn(banks);

            ResponseEntity<List<BankAccountDTO>> response = orderController.getSupportedBanks();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().size());
        }

        @Test
        @DisplayName("Order dengan bank transfer dan kode bank berhasil")
        void orderWithBankTransfer_Success() {
            BankAccountDTO selectedBank = new BankAccountDTO("BCA", "Bank Central Asia", "1234567890", "Toko E-Commerce");
            
            orderDTO.getPayment().setBankCode("BCA");
            orderDTO.getPayment().setBankName("Bank Central Asia");
            orderDTO.getPayment().setBankAccountNumber("1234567890");
            orderDTO.getPayment().setBankAccountName("Toko E-Commerce");

            when(featureConfig.isBankTransferEnabled()).thenReturn(true);
            when(featureConfig.isBankSupported("BCA")).thenReturn(true);
            when(featureConfig.getBankAccount("BCA")).thenReturn(selectedBank);
            when(orderService.placeOrderWithBankTransfer(testEmail, testCartId, selectedBank)).thenReturn(orderDTO);

            ResponseEntity<OrderDTO> response = orderController.orderWithBankTransfer(testEmail, testCartId, "BCA");

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("BCA", response.getBody().getPayment().getBankCode());
            assertEquals("1234567890", response.getBody().getPayment().getBankAccountNumber());
        }

        @Test
        @DisplayName("Order dengan kode bank tidak valid ditolak")
        void orderWithBankTransfer_InvalidBankCode_Rejected() {
            when(featureConfig.isBankTransferEnabled()).thenReturn(true);
            when(featureConfig.isBankSupported("UNKNOWN")).thenReturn(false);
            when(featureConfig.getSupportedBanksMessage()).thenReturn("Supported banks: BCA, BNI, MANDIRI");

            APIException exception = assertThrows(APIException.class,
                () -> orderController.orderWithBankTransfer(testEmail, testCartId, "UNKNOWN"));

            assertTrue(exception.getMessage().contains("not supported") || exception.getMessage().contains("Supported banks"));
        }

        private BankAccountDTO createBankDTO(String code, String name) {
            BankAccountDTO bank = new BankAccountDTO();
            bank.setBankCode(code);
            bank.setBankName(name);
            return bank;
        }
    }
}
