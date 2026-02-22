package com.app.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.app.entites.Wishlist;

@Repository
public interface WishlistRepo extends JpaRepository<Wishlist, Long> {

    @Query("SELECT w FROM Wishlist w WHERE w.user.userId = ?1")
    Optional<Wishlist> findByUserId(Long userId);

    @Query("SELECT w FROM Wishlist w WHERE w.user.email = ?1")
    Optional<Wishlist> findByUserEmail(String email);
}
