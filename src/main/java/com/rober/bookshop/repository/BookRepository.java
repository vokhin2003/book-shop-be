package com.rober.bookshop.repository;

import com.rober.bookshop.model.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    @Modifying
    @Query("UPDATE Book b SET b.quantity = :quantity WHERE b.id = :id")
    void updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

}
