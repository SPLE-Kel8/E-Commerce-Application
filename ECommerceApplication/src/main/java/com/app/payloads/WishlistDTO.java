package com.app.payloads;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wishlist DTO - response object for wishlist data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDTO {

    private Long wishlistId;
    private Long userId;
    private int totalItems;
    private List<WishlistItemDTO> items = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
