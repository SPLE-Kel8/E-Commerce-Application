package com.app.controllers;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.app.exceptions.APIException;
import com.app.exceptions.MyGlobalExceptionHandler;
import com.app.payloads.CreateProductReviewDTO;
import com.app.payloads.ProductReviewDTO;
import com.app.payloads.ProductReviewResponse;
import com.app.security.AuthUtil;
import com.app.services.ProductReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductReviewController Integration Tests - MockMvc Standalone")
class ProductReviewControllerIntegrationTest {

    @Mock
    private ProductReviewService productReviewService;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private ProductReviewController productReviewController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private ProductReviewDTO productReviewDTO;
    private ProductReviewResponse productReviewResponse;
    private CreateProductReviewDTO createProductReviewDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productReviewController)
                .setControllerAdvice(new MyGlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        productReviewDTO = new ProductReviewDTO();
        productReviewDTO.setReviewId(100L);
        productReviewDTO.setProductId(10L);
        productReviewDTO.setUserId(1L);
        productReviewDTO.setUserEmail("user@test.com");
        productReviewDTO.setRating(5);
        productReviewDTO.setComment("Produk bagus sekali");
        productReviewDTO.setCreatedAt(LocalDateTime.now());
        productReviewDTO.setUpdatedAt(LocalDateTime.now());

        productReviewResponse = new ProductReviewResponse();
        productReviewResponse.setContent(Arrays.asList(productReviewDTO));
        productReviewResponse.setPageNumber(0);
        productReviewResponse.setPageSize(5);
        productReviewResponse.setTotalElements(1L);
        productReviewResponse.setTotalPages(1);
        productReviewResponse.setLastPage(true);
        productReviewResponse.setAverageRating(5.0);
        productReviewResponse.setReviewCount(1L);

        createProductReviewDTO = new CreateProductReviewDTO();
        createProductReviewDTO.setRating(5);
        createProductReviewDTO.setComment("Produk bagus sekali");
    }

    @Test
    @DisplayName("GET /api/public/products/{productId}/reviews returns 200 and review summary")
    void getReviewsByProduct_Returns200() throws Exception {
        when(productReviewService.getReviewsByProduct(10L, 0, 5, "createdAt", "desc"))
                .thenReturn(productReviewResponse);

        mockMvc.perform(get("/api/public/products/{productId}/reviews", 10L)
                        .param("pageNumber", "0")
                        .param("pageSize", "5")
                        .param("sortBy", "createdAt")
                        .param("sortOrder", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewCount").value(1))
                .andExpect(jsonPath("$.averageRating").value(5.0))
                .andExpect(jsonPath("$.content[0].comment").value("Produk bagus sekali"));
    }

    @Test
    @DisplayName("POST /api/public/users/{email}/products/{productId}/reviews returns 201")
    void createReview_Returns201() throws Exception {
        doNothing().when(authUtil).validateUserAccess("user@test.com");
        when(productReviewService.createReview("user@test.com", 10L, createProductReviewDTO)).thenReturn(productReviewDTO);

        mockMvc.perform(post("/api/public/users/{email}/products/{productId}/reviews", "user@test.com", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createProductReviewDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewId").value(100))
                .andExpect(jsonPath("$.rating").value(5));

        verify(authUtil).validateUserAccess("user@test.com");
    }

    @Test
    @DisplayName("POST /api/public/users/{email}/products/{productId}/reviews returns 400 when access denied")
    void createReview_AccessDenied_Returns400() throws Exception {
        doThrow(new APIException("Access denied: You can only access your own resources"))
                .when(authUtil).validateUserAccess("other@test.com");

        mockMvc.perform(post("/api/public/users/{email}/products/{productId}/reviews", "other@test.com", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createProductReviewDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Access denied: You can only access your own resources"))
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    @DisplayName("GET /api/public/users/{email}/products/{productId}/reviews/me returns 200")
    void getMyReviewForProduct_Returns200() throws Exception {
        doNothing().when(authUtil).validateUserAccess("user@test.com");
        when(productReviewService.getReviewByUserAndProduct("user@test.com", 10L)).thenReturn(productReviewDTO);

        mockMvc.perform(get("/api/public/users/{email}/products/{productId}/reviews/me", "user@test.com", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userEmail").value("user@test.com"))
                .andExpect(jsonPath("$.productId").value(10));
    }

    @Test
    @DisplayName("PUT /api/public/users/{email}/products/{productId}/reviews/{reviewId} returns 200")
    void updateReview_Returns200() throws Exception {
        CreateProductReviewDTO updateDTO = new CreateProductReviewDTO(4, "Update review");

        ProductReviewDTO updatedDTO = new ProductReviewDTO();
        updatedDTO.setReviewId(100L);
        updatedDTO.setProductId(10L);
        updatedDTO.setUserId(1L);
        updatedDTO.setUserEmail("user@test.com");
        updatedDTO.setRating(4);
        updatedDTO.setComment("Update review");

        doNothing().when(authUtil).validateUserAccess("user@test.com");
        when(productReviewService.updateReview("user@test.com", 10L, 100L, updateDTO)).thenReturn(updatedDTO);

        mockMvc.perform(put("/api/public/users/{email}/products/{productId}/reviews/{reviewId}", "user@test.com", 10L, 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Update review"));
    }

    @Test
    @DisplayName("DELETE /api/public/users/{email}/products/{productId}/reviews/{reviewId} returns 200")
    void deleteReview_Returns200() throws Exception {
        doNothing().when(authUtil).validateUserAccess("user@test.com");
        when(productReviewService.deleteReview("user@test.com", 10L, 100L)).thenReturn("Review deleted successfully");

        mockMvc.perform(delete("/api/public/users/{email}/products/{productId}/reviews/{reviewId}", "user@test.com", 10L, 100L))
                .andExpect(status().isOk())
                .andExpect(content().string("Review deleted successfully"));
    }
}
