package com.rober.bookshop.repository;

import com.rober.bookshop.model.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long>, JpaSpecificationExecutor<Favorite> {

    @Transactional
    @Modifying
    @Query("delete from Favorite f where f.id = ?1")
    void deleteFavoriteById(Long id);

    Favorite findFavoriteByUserIdAndBookId(Long userId, Long bookId);
}
