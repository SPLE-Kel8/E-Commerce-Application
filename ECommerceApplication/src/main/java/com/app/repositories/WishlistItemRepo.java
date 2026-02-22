package com.app.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.app.entites.WishlistItem;

@Repository
public interface WishlistItemRepo extends JpaRepository<WishlistItem, Long> {

    @Query("SELECT wi FROM WishlistItem wi WHERE wi.wishlist.wishlistId = ?1")
    List<WishlistItem> findByWishlistId(Long wishlistId);

    @Query("SELECT wi FROM WishlistItem wi WHERE wi.wishlist.wishlistId = ?1 AND wi.product.productId = ?2")
    Optional<WishlistItem> findByWishlistIdAndProductId(Long wishlistId, Long productId);

    @Query("SELECT COUNT(wi) FROM WishlistItem wi WHERE wi.wishlist.wishlistId = ?1")
    int countByWishlistId(Long wishlistId);

    @Query("SELECT CASE WHEN COUNT(wi) > 0 THEN true ELSE false END FROM WishlistItem wi WHERE wi.wishlist.wishlistId = ?1 AND wi.product.productId = ?2")
    boolean existsByWishlistIdAndProductId(Long wishlistId, Long productId);

    void deleteByWishlistWishlistId(Long wishlistId);
}
