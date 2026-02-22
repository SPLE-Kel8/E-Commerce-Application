package com.app.payloads;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WishlistItem DTO - response object for wishlist item data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemDTO {

    private Long wishlistItemId;
    private Long productId;
    private String productName;
    private String description;
    private String image;
    private double price;
    private double discount;
    private double specialPrice;
    private boolean inStock;
    private String categoryName;
    private LocalDateTime addedAt;
}
